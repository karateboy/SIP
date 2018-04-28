package models
import play.api._
import akka.actor._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import com.github.nscala_time.time.Imports._
import play.api.Play.current
import ModelHelper._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import scalikejdbc._
import org.mongodb.scala.model._
import org.mongodb.scala.bson._

case class AvgRecord(dp_no: String, mt: MonitorType.Value, dateTime: DateTime, value: Option[Double], status: Option[String])
object CopyStep extends Enumeration {
  val hour = Value
  val min = Value
}
object DataCopyer {
  case object StartCopy
  case class CopyRange(begin: DateTime, end: DateTime)

  var hourCopyer: ActorRef = _
  var minCopyer: ActorRef = _
  def startup() = {
    hourCopyer = Akka.system.actorOf(Props(classOf[DataCopyer], CopyStep.hour), name = "hourCopyer")
    //minCopyer = Akka.system.actorOf(Props(classOf[DataCopyer], CopyStep.min), name = "minCopyer")
  }

  val unknownMonitor = "Unknown"
  val monitorMap = Map(
    "D001" -> "台塑六輕工業園區#彰化縣大城站",
    "D003" -> "台塑六輕工業園區#嘉義縣東石站",
    "D005" -> "台塑六輕工業園區#雲林縣褒忠站",
    "D006" -> "台塑六輕工業園區#雲林縣崙背站",
    "D007" -> "台塑六輕工業園區#雲林縣四湖站",
    "D008" -> "台塑六輕工業園區#雲林縣東勢站",
    "D009" -> "台塑六輕工業園區#雲林縣麥寮站",
    "D109" -> "台塑六輕工業園區#雲林縣麥寮站",
    "D010" -> "台塑六輕工業園區#雲林縣台西站",
    "D110" -> "台塑六輕工業園區#雲林縣台西站",
    "D011" -> "台塑六輕工業園區#雲林縣土庫站",
    "D012" -> "台塑六輕工業園區#雲林縣西螺站")

  val mtMap = Map(
    "A214" -> "PM10",
    "A222" -> "二氧化硫",
    "A223" -> "氮氧化物",
    "A283" -> "一氧化氮",
    "A293" -> "二氧化氮",
    "A224" -> "一氧化碳",
    "A225" -> "臭氧",
    "A224" -> "一氧化碳",
    "A225" -> "臭氧",
    "A227" -> "總碳氫化合物",
    "C211" -> "風速",
    "C212" -> "風向",
    "C213" -> "降雨量",
    "C214" -> "溫度",
    "C215" -> "相對濕度",
    "U201" -> "乙烷",
    "U202" -> "乙烯",
    "U203" -> "丙烷",
    "U204" -> "丙烯",
    "U205" -> "異丁烷",
    "U206" -> "正丁烷",
    "U207" -> "乙炔",
    "U208" -> "反2-丁烯",
    "U209" -> "1-丁烯",
    "U210" -> "順2-丁烯",
    "U211" -> "環戊烷",
    "U212" -> "異戊烷",
    "U213" -> "正戊烷",
    "U214" -> "反2-戊烯",
    "U215" -> "1-戊烯",
    "U216" -> "順2-戊烯",
    "U217" -> "2,2-二甲基丁烷",
    "U218" -> "2,3-二甲基丁烷",
    "U219" -> "2-甲基戊烷",
    "U220" -> "3-甲基戊烷",
    "U221" -> "異戊二烯",
    "U222" -> "正己烷",
    "U223" -> "甲基環戊烷",
    "U224" -> "2,4-二甲基戊烷",
    "U225" -> "苯",
    "U226" -> "環己烷",
    "U227" -> "2-甲基己烷",
    "U228" -> "2,3-二甲基戊烷",
    "U229" -> "3-甲基己烷",
    "U230" -> "2,2,4-三甲基戊烷",
    "U231" -> "正庚烷",
    "U232" -> "甲基環己烷",
    "U233" -> "2,3,4-三甲基戊烷",
    "U234" -> "甲苯",
    "U235" -> "2-甲基庚烷",
    "U236" -> "3-甲基庚烷",
    "U237" -> "正辛烷",
    "U238" -> "乙苯",
    "U239" -> "間,對二甲苯",
    "U240" -> "苯乙烯",
    "U241" -> "鄰二甲苯",
    "U242" -> "正壬烷",
    "U243" -> "異丙基苯",
    "U244" -> "正丙基苯",
    "U245" -> "間-乙基甲苯",
    "U246" -> "對-乙基甲苯",
    "U247" -> "1,3,5-三甲基苯",
    "U248" -> "鄰-乙基甲苯",
    "U249" -> "1,2,4-三甲基苯",
    "U250" -> "葵烷",
    "U251" -> "1,2,3-三甲基苯",
    "U252" -> "間-二乙基苯",
    "U253" -> "對-二乙基苯",
    "U254" -> "正十一烷")

  def getCopyRange(step: CopyStep.Value) = {
    Logger.debug(s"getCopyRange $step")
    val latestF = step match {
      case CopyStep.hour =>
        SysConfig.get(SysConfig.AVGHR_LAST)
      case CopyStep.min =>
        SysConfig.get(SysConfig.AVGR_LAST)
    }

    for { latest <- latestF } yield {
      val begin = step match {
        case CopyStep.hour =>
          new DateTime(latest.asDateTime().toDate())
        case CopyStep.min =>
          new DateTime(latest.asDateTime().toDate())
      }

      val now = DateTime.now()

      val end =
        step match {
          case CopyStep.hour =>
            if (begin + 1.day > now)
              now
            else
              (begin + 1.day).withMillisOfDay(0)

          case CopyStep.min =>
            if (begin + 1.day > now)
              now
            else
              (begin + 1.day).withMillisOfDay(0)
        }
      (begin, end)
    }
  }

  def copyDbRange(begin: DateTime, end: DateTime, step: CopyStep.Value) {
    val tab = step match {
      case CopyStep.hour =>
        SQLSyntax.createUnsafely(s"A_AVGHR${begin.getYear}")
      case CopyStep.min =>
        SQLSyntax.createUnsafely(s"A_AVGR${begin.getYear}")
    }

    val year = s"${begin.getYear}"
    val month = "%02d".format(begin.getMonthOfYear)
    val day = "%02d".format(begin.getDayOfMonth)
    Logger.info(s"copy $step ${begin.toString("YY/MM/dd HH:mm")} to ${end.toString("YY/MM/dd HH:mm")}")

    val result = DB readOnly {
      implicit session =>

        sql"""
          Select DP_NO, ITEM, M_YEAR, M_Month, M_DAY, M_TIME, M_VAL, CODE2
          From ${tab}
          Where M_YEAR=${year} and M_Month=${month} and M_DAY=${day} 
        """.map {
          rs =>
            import org.joda.time.format.DateTimeFormat
            val dp_no = monitorMap.getOrElse(rs.string("DP_NO"), unknownMonitor)
            val item = rs.string("ITEM")
            val mtStr = item.take(1) + "2" + item.drop(2)
            val mt = MonitorType.withName(mtMap(mtStr))
            val dtStr = s"${rs.string("M_YEAR")}/${rs.string("M_Month")}/${rs.string("M_DAY")}/${rs.string("M_TIME")}"
            val dt = DateTime.parse(dtStr, DateTimeFormat.forPattern("YYYY/MM/dd/HHmmss"))
            val v = rs.doubleOpt("M_VAL")
            val s = rs.stringOpt("CODE2")
            AvgRecord(dp_no, mt, dt, v, s)
        }.list().apply()
    }

    val recordList = result filter {
      hr =>
        (hr.dp_no != unknownMonitor && hr.value.isDefined && hr.status.isDefined) &&
          (hr.dateTime >= begin && hr.dateTime < end)
    }

    def updateDB = {
      import scala.collection.mutable.Map
      val recordMap = Map.empty[Monitor.Value, Map[DateTime, Map[MonitorType.Value, (Double, String)]]]
      for (record <- recordList) {
        try {
          val monitor = Monitor.withName(record.dp_no)
          val monitorType = record.mt
          val timeMap = recordMap.getOrElseUpdate(monitor, Map.empty[DateTime, Map[MonitorType.Value, (Double, String)]])
          val mtMap = timeMap.getOrElseUpdate(record.dateTime, Map.empty[MonitorType.Value, (Double, String)])
          mtMap.put(record.mt, (record.value.get, record.status.get))
        } catch {
          case ex: Throwable =>
            Logger.error("skip invalid record ", ex)
        }
      }

      val updateModels =
        for {
          monitorMap <- recordMap
          monitor = monitorMap._1
          timeMaps = monitorMap._2
          dateTime <- timeMaps.keys.toList.sorted
          mtMaps = timeMaps(dateTime) if (!mtMaps.isEmpty)
          doc = Record.toDocument(monitor, dateTime, mtMaps.toList)
          updateList = doc.toList.map(kv => Updates.set(kv._1, kv._2)) if !updateList.isEmpty
        } yield {
          UpdateOneModel(
            Filters.eq("_id", doc("_id")),
            Updates.combine(updateList: _*), UpdateOptions().upsert(true))
        }
      Logger.info(s"update $step ${updateModels.size} records")

      val collection =
        step match {
          case CopyStep.hour =>
            MongoDB.database.getCollection(Record.HourCollection)
          case CopyStep.min =>
            MongoDB.database.getCollection(Record.MinCollection)
        }
      val f2 = collection.bulkWrite(updateModels.toList, BulkWriteOptions().ordered(false)).toFuture()
      f2.onFailure(errorHandler)
      waitReadyResult(f2)

      step match {
        case CopyStep.hour =>
          SysConfig.set(SysConfig.AVGHR_LAST, BsonDateTime(end))
        case CopyStep.min =>
          SysConfig.set(SysConfig.AVGR_LAST, BsonDateTime(end))
      }
    }

    if (!recordList.isEmpty)
      updateDB
  }

}

class DataCopyer(step: CopyStep.Value) extends Actor with ActorLogging {
  import DataCopyer._

  Logger.info(s"$step Copyer start...")
  val timer = {
    import scala.concurrent.duration._
    step match {
      case CopyStep.hour =>
        Akka.system.scheduler.schedule(Duration(5, SECONDS), Duration(5, MINUTES), self, StartCopy)
      case CopyStep.min =>
        Akka.system.scheduler.schedule(Duration(5, SECONDS), Duration(1, MINUTES), self, StartCopy)
    }

  }

  def receive = handler(false)

  def handler(copying: Boolean): Receive = {
    case StartCopy =>
      if (!copying) {
        for ((begin, end) <- getCopyRange(step)) {
          val minDuration = step match {
            case CopyStep.hour =>
              1.hour
            case CopyStep.min =>
              1.minute
          }

          if (begin < DateTime.now() && begin + minDuration <= end) {
            self ! CopyRange(begin, end)
            context become handler(true)
          }
        }
      }
    case CopyRange(begin, end) =>
      copyDbRange(begin, end, step)
      val now = DateTime.now()
      val nextBegin = end
      var nextEnd =
        step match {
          case CopyStep.hour =>
            end + 1.day
          case CopyStep.min =>
            end + 1.day
        }

      if (nextEnd > now)
        nextEnd = now

      val minDuration = step match {
        case CopyStep.hour =>
          1.hour
        case CopyStep.min =>
          1.minute
      }
      if (nextBegin < now && nextBegin + minDuration <= nextEnd) {
        self ! CopyRange(nextBegin, nextEnd)
      } else {
        context become handler(false)
      }
  }

  override def postStop(): Unit = {
    timer.cancel()
  }
}