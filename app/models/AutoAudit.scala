package models
import play.api._
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import org.mongodb.scala.bson._
import org.mongodb.scala.model._

case class MinMaxCfg(
  enabled: Boolean,
  id:      MonitorType.Value,
  min:     Double,
  max:     Double)

case class MinMaxRule(
  enabled:      Boolean,
  monitorTypes: Seq[MinMaxCfg]) {

  val lead = 'a'
  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true
    else {
      var pass = true
      for (cfg <- monitorTypes) {
        val mt = cfg.id

        if (mtMaps.contains(mt) && MonitorStatus.isValid(mtMaps(mt)._2)) {
          val mt_value = mtMaps(mt)._1
          val mt_status = mtMaps(mt)._2

          if (mt_value > cfg.max || mt_value <= cfg.min) {
            Record.updateRecordStatus(monitor, dateTime.getMillis, mt, lead + mt_status.substring(1))(Record.HourCollection)
            if (alarm)
              Alarm.log(monitor, mt,
                s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mt, Some(mt_value))} 違反極大極小值稽核")

            pass = false
          }
        }
      }
      pass
    }
  }
}

object MinMaxRule {
  implicit val minMaxCfgRead = Json.reads[MinMaxCfg]
  implicit val minMaxCfgWrite = Json.writes[MinMaxCfg]
  implicit val minMaxRuleWrite = Json.writes[MinMaxRule]
  implicit val minMaxRuleRead = Json.reads[MinMaxRule]

  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield MinMaxCfg(false, mt, 100, 0)

    MinMaxRule(false, cfgs)
  }
}

case class CompareRule(
  enabled: Boolean) {

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    ???
  }
}

object CompareRule {
  implicit val compareRuleRead = Json.reads[CompareRule]
  implicit val compareRuleWrite = Json.writes[CompareRule]

  val default = CompareRule(false)
}

case class DifferenceRule(
  enabled:      Boolean,
  multiplier:   Double,
  monitorTypes: Seq[MonitorType.Value]) {
  val lead = 'c'

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(monitorTypes.toList, monitor, dateTime - 24.hour, dateTime)
    val mtAvgStdPairs =
      for {
        mt <- monitorTypes if historyMap.contains(mt)
        mt_records = historyMap(mt).filter { rec => MonitorStatus.isValid(rec.status) }.map { _.value }
      } yield {
        val count = mt_records.length
        val avg = mt_records.sum / count
        val std = Math.sqrt(mt_records.map { r => (r - avg) * (r - avg) }.sum / count)
        mt -> (avg, std)
      }

    val avgStdMap = Map(mtAvgStdPairs: _*)

    for {
      mt <- monitorTypes if mtMaps.contains(mt)
      mr_record = mtMaps(mt) if (MonitorStatus.isValid(mr_record._2))
    } {
      val v = mr_record._1
      val (avg, std) = avgStdMap(mt)
      if (Math.abs(v - avg) > multiplier * std) {
        Record.updateRecordStatus(monitor, dateTime.getMillis, mt, lead + mr_record._2.substring(1))(Record.HourCollection)
        if (alarm)
          Alarm.log(monitor, mt,
            s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mt, Some(v))} 違反單調性稽核")
        pass = false
      }
    }

    pass
  }
}

object DifferenceRule {
  implicit val differenceRuleRead = Json.reads[DifferenceRule]
  implicit val differenceRuleWrite = Json.writes[DifferenceRule]

  def default =
    DifferenceRule(false, 3, MonitorType.mtvList)
}

case class SpikeCfg(
  enabled: Boolean,
  id:      MonitorType.Value,
  abs:     Double) {
  val lead = 'd'
}
case class SpikeRule(
  enabled:      Boolean,
  monitorTypes: Seq[SpikeCfg]) {
  val lead = 'd'
  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val monitorTypeSeq = monitorTypes map { _.id }
    val historyMap = Record.getRecordMap(Record.HourCollection)(monitorTypeSeq.toList, monitor, dateTime - 2.hour, dateTime)

    for (mtcfg <- monitorTypes if mtMaps.contains(mtcfg.id) && historyMap.contains(mtcfg.id)) {
      val pre_records = historyMap(mtcfg.id)

      val mt_rec = mtMaps(mtcfg.id)
      val pre_mt_rec = pre_records.filter(r => MonitorStatus.isValid(r.status))
      if (MonitorStatus.isValid(mt_rec._2) && pre_mt_rec.length == 2) {
        val avg = (pre_mt_rec(0).value + mt_rec._1) / 2
        val v = pre_mt_rec(1).value
        if (Math.abs(v - avg) > mtcfg.abs) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mtcfg.id, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mtcfg.id,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mtcfg.id, Some(v))} 突波高值稽核")
        }
      }
    }

    pass
  }

}

object SpikeRule {
  implicit val spikeCfgRead = Json.reads[SpikeCfg]
  implicit val spikeRuleRead = Json.reads[SpikeRule]
  implicit val spikeCfgWrite = Json.writes[SpikeCfg]
  implicit val spikeRuleWrite = Json.writes[SpikeRule]

  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield SpikeCfg(false, mt, 10)
    SpikeRule(false, cfgs)
  }
}

case class PersistenceRule(
  enabled: Boolean,
  same:    Int) {
  val lead = 'e'

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, dateTime - (same - 1).hour, dateTime)

    for (mt <- MonitorType.mtvList if mtMaps.contains(mt) && historyMap.contains(mt)) {
      val mt_rec = mtMaps(mt)
      if (MonitorStatus.isValid(mt_rec._2)) {
        val pre_mt_rec = historyMap(mt).filter(r => MonitorStatus.isValid(r.status)).filter(r => r.value == mt_rec._1)
        if (pre_mt_rec.length == same - 1) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mt, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mt,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mt, Some(mt_rec._1))} 違反持續性稽核")
        }
      }
    }

    pass
  }
}

object PersistenceRule {
  implicit val persistenceRuleRead = Json.reads[PersistenceRule]
  implicit val persistenceRuleWrite = Json.writes[PersistenceRule]

  val default = PersistenceRule(false, 3)
}

case class MonoCfg(
  enabled: Boolean,
  id:      MonitorType.Value,
  abs:     Double) {
}

case class MonoRule(enabled: Boolean, count: Int,
                    monitorTypes: Seq[MonoCfg]) {
  val lead = 'f'

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, dateTime - (count - 1).hour, dateTime)

    for (mtcfg <- monitorTypes if historyMap.contains(mtcfg.id) && mtMaps.contains(mtcfg.id)) {
      val mt_rec = mtMaps(mtcfg.id)
      val pre_rec = historyMap(mtcfg.id).filter(r => MonitorStatus.isValid(r.status))
      if (MonitorStatus.isValid(mt_rec._2) && pre_rec.length == count - 1) {
        val values = pre_rec.map(_.value) ++ List(mt_rec._1)
        val max = values.max
        val min = values.min
        if ((max - min) < mtcfg.abs) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mtcfg.id, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mtcfg.id,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mtcfg.id, Some(mt_rec._1))} 違反一致姓稽核")
        }
      }
    }

    pass
  }
}

object MonoRule {
  implicit val monoCfgRead = Json.reads[MonoCfg]
  implicit val monoCfgWrite = Json.writes[MonoCfg]
  implicit val monoRuleRead = Json.reads[MonoRule]
  implicit val monoRuleWrite = Json.writes[MonoRule]

  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield MonoCfg(false, mt, 10)
    MonoRule(false, 3, cfgs)
  }
}

case class TwoHourRule(enabled: Boolean, monitorTypes: Seq[MonoCfg]) {
  val lead = 'g'
  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, dateTime - 1.hour, dateTime)

    for (mtcfg <- monitorTypes if mtMaps.contains(mtcfg.id) && historyMap.contains(mtcfg.id)) {
      val mt = mtcfg.id
      val mt_rec = mtMaps(mt)
      val pre_rec = historyMap(mt).filter(r => MonitorStatus.isValid(r.status))
      if (MonitorStatus.isValid(mt_rec._2) && pre_rec.length == 1) {
        if (Math.abs(pre_rec(0).value - mt_rec._1) > mtcfg.abs) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mtcfg.id, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mtcfg.id,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mtcfg.id, Some(mt_rec._1))} 違反小時值變換驗證")
        }
      }
    }

    pass
  }
}

object TwoHourRule {
  import MonoRule._
  implicit val read = Json.reads[TwoHourRule]
  implicit val write = Json.writes[TwoHourRule]
  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield MonoCfg(false, mt, 10)

    TwoHourRule(false, cfgs)
  }
}

case class ThreeHourCfg(
  enabled: Boolean,
  id:      MonitorType.Value,
  abs:     Double,
  percent: Double)

case class ThreeHourRule(enabled: Boolean, monitorTypes: Seq[ThreeHourCfg]) {
  val lead = 'h'

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, dateTime - 2.hour, dateTime)

    for (mtcfg <- monitorTypes if mtMaps.contains(mtcfg.id) && historyMap.contains(mtcfg.id)) {
      val mt = mtcfg.id
      val mt_rec = mtMaps(mt)
      val pre_rec = historyMap(mt).filter(r => MonitorStatus.isValid(r.status))
      if (MonitorStatus.isValid(mt_rec._2) && pre_rec.length == 2) {
        val values = pre_rec.map(_.value) ++ List(mt_rec._1)
        val abs_percent =
          for (v1 <- values.zipWithIndex.dropRight(1)) yield {
            val v2 = values(v1._2)
            (Math.abs(v1._1 - v2), Math.abs((1 - v1._1 / v2) * 100))
          }
        val overs = abs_percent.filter(v => v._1 > mtcfg.abs && v._2 > mtcfg.percent)
        if (overs.length == 2) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mtcfg.id, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mtcfg.id,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mtcfg.id, Some(mt_rec._1))} 違反三小時值變換驗證")
        }
      }
    }

    pass
  }
}

object ThreeHourRule {
  implicit val thcfgRead = Json.reads[ThreeHourCfg]
  implicit val thcfgWrite = Json.writes[ThreeHourCfg]
  implicit val reads = Json.reads[ThreeHourRule]
  implicit val writes = Json.writes[ThreeHourRule]

  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield ThreeHourCfg(false, mt, 10, 10)
    ThreeHourRule(false, cfgs)
  }
}

case class FourHourCfg(
  enabled: Boolean,
  id:      MonitorType.Value,
  abs:     Double)

case class FourHourRule(enabled: Boolean, monitorTypes: Seq[FourHourCfg]) {
  val lead = 'i'

  def check(monitor: Monitor.Value, dateTime: DateTime, mtMaps: Map[MonitorType.Value, (Double, String)], alarm: Boolean) = {
    if (!enabled)
      true

    var pass = true
    val historyMap = Record.getRecordMap(Record.HourCollection)(MonitorType.mtvList, monitor, dateTime - 3.hour, dateTime)

    for (mtcfg <- monitorTypes if mtMaps.contains(mtcfg.id) && historyMap.contains(mtcfg.id)) {
      val mt = mtcfg.id
      val mt_rec = mtMaps(mt)
      val pre_rec = historyMap(mt).filter(r => MonitorStatus.isValid(r.status))
      if (MonitorStatus.isValid(mt_rec._2) && pre_rec.length == 3) {
        val values = pre_rec.map(_.value) ++ List(mt_rec._1)
        val avg = values.sum / 4
        if (avg > mtcfg.abs) {
          pass = false
          Record.updateRecordStatus(monitor, dateTime.getMillis, mtcfg.id, lead + mt_rec._2.substring(1))(Record.HourCollection)
          if (alarm)
            Alarm.log(monitor, mtcfg.id,
              s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mtcfg.id, Some(mt_rec._1))} 違反四小時值變換驗證")
        }
      }
    }

    pass
  }
}

object FourHourRule {
  implicit val thcfgRead = Json.reads[FourHourCfg]
  implicit val thcfgWrite = Json.writes[FourHourCfg]
  implicit val reads = Json.reads[FourHourRule]
  implicit val writes = Json.writes[FourHourRule]

  def default = {
    val cfgs = for (mt <- MonitorType.mtvList) yield FourHourCfg(false, mt, 10)
    FourHourRule(false, Seq.empty[FourHourCfg])
  }
}

case class OverInternalStdMinRule(enabled: Boolean, threshold: Int) {
  val lead = 'j'

  def check(m: Monitor.Value): Boolean = {
    if (!enabled)
      true

    var pass = true
    val mCase = Monitor.map(m)

    val recordMap = Record.getRecordMap(Record.MinCollection)(MonitorType.mtvList, m, DateTime.now - 1.hour, DateTime.now)
    for {
      mt <- MonitorType.mtvList if recordMap.contains(mt) && MonitorType.map(mt).std_internal.isDefined
      std_internal = MonitorType.map(mt).std_internal.get
      mtRecords = recordMap(mt)
    } {
      val over = mtRecords.count(r =>
        MonitorStatus.isValid(r.status)
          && r.value > std_internal)

      if (over > threshold) {
        pass = false
        Alarm.log(m, mt, s"分鐘值超過內控")
      }

    }

    pass
  }

}

object OverInternalStdMinRule {
  implicit val reads = Json.reads[OverInternalStdMinRule]
  implicit val writes = Json.writes[OverInternalStdMinRule]
  val default = OverInternalStdMinRule(false, 20)
}

case class DataReadyMinRule(enabled: Boolean, overdue: Int) {
  val lead = 'k'

  /*
  def checkInvalid(m: Monitor.Value): Boolean = {
    if (!enabled)
      return false

    val currentMinOpt = Realtime.getLatestMonitorRecordTime(TableType.Min, m)
    if (currentMinOpt.isDefined) {
      val duetime = DateTime.now() - overdue.minutes
      if (currentMinOpt.get.toDateTime < duetime) {
        for (mt <- Monitor.map(m).monitorTypes) {
          val ar = Alarm2(m, DateTime.now, Src(mt), Level.ERR, "分鐘值回傳超時")
          try {
            Alarm2.insertAlarm(ar)
          } catch {
            case ex: Exception =>
            // Skip duplicate alarm
          }
        }
        return true
      }
    }

    false
  }
  *
  */
}

object DataReadyMinRule {
  implicit val reads = Json.reads[DataReadyMinRule]
  implicit val writes = Json.writes[DataReadyMinRule]
  val default = DataReadyMinRule(false, 10)
}

case class AutoAudit(
  minMaxRule:             MinMaxRule,
  compareRule:            CompareRule,
  differenceRule:         DifferenceRule,
  spikeRule:              SpikeRule,
  persistenceRule:        PersistenceRule,
  monoRule:               MonoRule,
  twoHourRule:            TwoHourRule,
  threeHourRule:          ThreeHourRule,
  fourHourRule:           FourHourRule,
  overInternalStdMinRule: OverInternalStdMinRule,
  dataReadyMinRule:       DataReadyMinRule) {
}

object AutoAudit {
  implicit val autoAuditRead = Json.reads[AutoAudit]
  implicit val autoAuditWrite = Json.writes[AutoAudit]

  val default = AutoAudit(
    MinMaxRule.default,
    CompareRule.default,
    DifferenceRule.default,
    SpikeRule.default,
    PersistenceRule.default,
    MonoRule.default,
    TwoHourRule.default,
    ThreeHourRule.default,
    FourHourRule.default,
    OverInternalStdMinRule.default,
    DataReadyMinRule.default)

  val map = Map(
    'a' -> "極大極小值",
    'b' -> "合理性",
    'c' -> "單調性",
    'd' -> "突波高值",
    'e' -> "持續性",
    'f' -> "一致性",
    'g' -> "小時測值變化驗證",
    'h' -> "三小時變化測值驗證",
    'i' -> "四小時變化測值驗證",
    'j' -> "分鐘值超過內控",
    'k' -> "分鐘值回傳超時")

  def audit(recordMap: scala.collection.mutable.Map[Monitor.Value, scala.collection.mutable.Map[DateTime, scala.collection.mutable.Map[MonitorType.Value, (Double, String)]]], alarm: Boolean) = {
    /*
    for {
      monitorMap <- recordMap if Monitor.map(monitorMap._1).autoAudit.isDefined
      monitor = monitorMap._1
      autoAudit = Monitor.map(monitor).autoAudit.get
      timeMaps = monitorMap._2
      dateTime <- timeMaps.keys.toList.sorted
      mtMaps = timeMaps(dateTime) if (!mtMaps.isEmpty)
    } {
      if (autoAudit.minMaxRule.enabled)
        autoAudit.minMaxRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.compareRule.enabled)
        autoAudit.compareRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.differenceRule.enabled)
        autoAudit.differenceRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.spikeRule.enabled)
        autoAudit.spikeRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.persistenceRule.enabled)
        autoAudit.persistenceRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.monoRule.enabled)
        autoAudit.monoRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.twoHourRule.enabled)
        autoAudit.twoHourRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.threeHourRule.enabled)
        autoAudit.threeHourRule.check(monitor, dateTime, mtMaps.toMap, alarm)

      if (autoAudit.fourHourRule.enabled)
        autoAudit.differenceRule.check(monitor, dateTime, mtMaps.toMap, alarm)
    }
    */
  }

  def audit2(monitor: Monitor.Value, recordSeq: Seq[Record.RecordList], alarm: Boolean) {
    /*
    for {
      record <- recordSeq
      time = new DateTime(record.time)
      autoAuditOpt = Monitor.map(monitor).autoAudit if autoAuditOpt.isDefined
      autoAudit = autoAuditOpt.get
    } {
      val mtPair = record.mtDataList map { mtRecord =>
        val mt = MonitorType.withName(mtRecord.mtName)
        mt -> (mtRecord.value, mtRecord.status)
      }
      val mtMap = mtPair.toMap

      if (autoAudit.minMaxRule.enabled)
        autoAudit.minMaxRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.compareRule.enabled)
        autoAudit.compareRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.differenceRule.enabled)
        autoAudit.differenceRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.spikeRule.enabled)
        autoAudit.spikeRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.persistenceRule.enabled)
        autoAudit.persistenceRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.monoRule.enabled)
        autoAudit.monoRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.twoHourRule.enabled)
        autoAudit.twoHourRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.threeHourRule.enabled)
        autoAudit.threeHourRule.check(monitor, time, mtMap, alarm)

      if (autoAudit.fourHourRule.enabled)
        autoAudit.differenceRule.check(monitor, time, mtMap, alarm)
    }
    *
    */
  }
}