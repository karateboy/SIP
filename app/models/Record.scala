package models
import play.api._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import models._
import scala.concurrent.ExecutionContext.Implicits.global
import org.mongodb.scala._

case class Record(monitor: Monitor.Value, time: DateTime, value: Double, status: String)
case class Record2(value: Double, status: String)

//case class MTMap(mtMap: Map[MonitorType.Value, Record2])

object Record {
  type MTMap = Map[MonitorType.Value, Record2]
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import org.mongodb.scala.model.Indexes._
  implicit val writer = Json.writes[Record]

  val HourCollection = "hour_data"
  val MinCollection = "min_data"
  def init(colNames: Seq[String]) {
    import com.mongodb.client.model._
    if (!colNames.contains(HourCollection)) {
      val f = MongoDB.database.createCollection(HourCollection).toFuture()
      f.onFailure(errorHandler)
      f.onSuccess({
        case _: Seq[_] =>
          val indexOpt = new IndexOptions
          indexOpt.unique(true)
          val cf1 = MongoDB.database.getCollection(HourCollection).createIndex(ascending("time", "monitor"), indexOpt).toFuture()
          val cf2 = MongoDB.database.getCollection(HourCollection).createIndex(ascending("monitor", "time"), indexOpt).toFuture()
          cf1.onFailure(errorHandler)
          cf2.onFailure(errorHandler)
      })
    }

    if (!colNames.contains(MinCollection)) {
      val f = MongoDB.database.createCollection(MinCollection).toFuture()
      f.onFailure(errorHandler)
      f.onSuccess({
        case _: Seq[_] =>
          val cf1 = MongoDB.database.getCollection(MinCollection).createIndex(ascending("time", "monitor")).toFuture()
          val cf2 = MongoDB.database.getCollection(MinCollection).createIndex(ascending("monitor", "time")).toFuture()
          cf1.onFailure(errorHandler)
          cf2.onFailure(errorHandler)
      })
    }
  }

  def getDocKey(monitor: Monitor.Value, dt: DateTime) = {
    import org.mongodb.scala.bson._

    val bdt: BsonDateTime = dt
    Document("time" -> bdt, "monitor" -> monitor.toString)
  }

  def toDocument(monitor: Monitor.Value, dt: DateTime, dataList: List[(MonitorType.Value, (Double, String))]) = {
    import org.mongodb.scala.bson._
    val bdt: BsonDateTime = dt
    var doc = Document("_id" -> getDocKey(monitor, dt), "time" -> bdt, "monitor" -> monitor.toString)
    for {
      data <- dataList
      mt = data._1
      (v, s) = data._2
    } {
      doc = doc ++ Document(MonitorType.BFName(mt) -> Document("v" -> v, "s" -> s))
    }

    doc
  }

  def insertRecord(doc: Document)(colName: String) = {
    val col = MongoDB.database.getCollection(colName)
    val f = col.insertOne(doc).toFuture()
    f.onFailure({
      case ex: Exception => Logger.error(ex.getMessage, ex)
    })
    f
  }

  def upsertRecord(doc: Document)(colName: String) = {
    import org.mongodb.scala.model.UpdateOptions
    import org.mongodb.scala.bson.BsonString
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Updates._

    val col = MongoDB.database.getCollection(colName)

    val updateList = doc.toList.map(kv => set(kv._1, kv._2))

    val f = col.updateOne(equal("_id", doc("_id")), combine(updateList: _*), UpdateOptions().upsert(true)).toFuture()
    f.onFailure(errorHandler)

    f
  }

  def updateRecordStatus(monitor: Monitor.Value, dt: Long, mt: MonitorType.Value, status: String)(colName: String) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Updates._

    val col = MongoDB.database.getCollection(colName)
    val bdt = new BsonDateTime(dt)
    val fieldName = s"${MonitorType.BFName(mt)}.s"

    val f = col.updateOne(equal("_id", getDocKey(monitor, new DateTime(dt))), set(fieldName, status)).toFuture()
    f.onFailure({
      case ex: Exception => Logger.error(ex.getMessage, ex)
    })
    f
  }

  def getRecordMap(colName: String)(mtList: List[MonitorType.Value], monitor: Monitor.Value, startTime: DateTime, endTime: DateTime) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent._
    import scala.concurrent.duration._

    val col = MongoDB.database.getCollection(colName)
    val projFields = "monitor" :: "time" :: mtList.map { MonitorType.BFName(_) }
    val proj = include(projFields: _*)

    val f = col.find(and(equal("monitor", monitor.toString), gte("time", startTime.toDate()), lt("time", endTime.toDate()))).projection(proj).sort(ascending("time")).toFuture()
    val docs = waitReadyResult(f)

    val pairs =
      for {
        mt <- mtList
        mtBFName = MonitorType.BFName(mt)
      } yield {

        val list =
          for {
            doc <- docs
            monitor = Monitor.withName(doc("monitor").asString().getValue)
            time = doc("time").asDateTime()
            mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
            mtDoc = mtDocOpt.get.asDocument()
            v = mtDoc.get("v") if v.isDouble()
            s = mtDoc.get("s") if s.isString()
          } yield {
            Record(monitor, time, v.asDouble().doubleValue(), s.asString().getValue)
          }

        mt -> list
      }
    Map(pairs: _*)
  }

  import scala.concurrent._
  def getMonitorRecordMapF(colName: String)(mtList: List[MonitorType.Value], monitors: Seq[Monitor.Value], startTime: DateTime, endTime: DateTime): Future[Map[DateTime, Map[Monitor.Value, MTMap]]] = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent.duration._

    val col = MongoDB.database.getCollection(colName)
    val projFields = "monitor" :: "time" :: mtList.map { MonitorType.BFName(_) }
    val proj = include(projFields: _*)
    val monitorNames = monitors.map(_.toString())
    val f = col.find(and(in("monitor", monitorNames: _*), gte("time", startTime.toDate()), lt("time", endTime.toDate()))).projection(proj).sort(ascending("time")).toFuture()

    var timeMap = Map.empty[DateTime, Map[Monitor.Value, MTMap]]

    for (docs <- f) yield {
      val ret =
        for {
          doc <- docs
          time = new DateTime(doc("time").asDateTime().getValue)
        } yield {
          var monitorRecordMap = timeMap.getOrElse(time, Map.empty[Monitor.Value, MTMap])
          val monitor = Monitor.withName(doc("monitor").asString().getValue)
          val mtRecordPairs =
            for {
              mt <- mtList
              mtBFName = MonitorType.BFName(mt)
              mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
              mtDoc = mtDocOpt.get.asDocument()
              v = mtDoc.get("v") if v.isDouble()
              s = mtDoc.get("s") if s.isString()
            } yield {
              mt -> Record2(v.asDouble().getValue, s.asString().getValue)
            }
          val mtMap = mtRecordPairs.toMap
          monitorRecordMap = monitorRecordMap + (monitor -> mtMap)
          timeMap = timeMap + (time -> monitorRecordMap)
        }
      timeMap
    }
  }
  def resetAuditedRecord(colName: String)(mtList: List[MonitorType.Value], monitor: Monitor.Value, startTime: DateTime, endTime: DateTime) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._

    val f = getAuditRecordMapFuture(colName)(mtList, monitor, startTime, endTime)
    val map = waitReadyResult(f)
    val f2 =
      for {
        time_map <- map
        time = time_map._1
        recordMap = time_map._2
      } yield {
        val itF =
          for {
            mt_record <- recordMap
            mt = mt_record._1
            record = mt_record._2 if MonitorStatus.isAudited(record.status)
          } yield {
            val newStatus = "0" + record.status.substring(1)
            updateRecordStatus(monitor, time.getMillis, mt, newStatus)(colName)
          }
        itF.toSeq
      }
    import scala.concurrent._
    val f3 = f2.flatMap { x => x }
    Future.sequence(f2.flatMap { x => x })
  }

  def getAuditRecordMapFuture(colName: String)(mtList: List[MonitorType.Value], monitor: Monitor.Value, startTime: DateTime, endTime: DateTime) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent._
    import scala.concurrent.duration._

    val col = MongoDB.database.getCollection(colName)
    val projFields = "monitor" :: "time" :: mtList.map { MonitorType.BFName(_) }
    val proj = include(projFields: _*)
    val audited = or(mtList.map { mt => Filters.regex(MonitorType.BFName(mt) + ".s", "^[a-zA-Z]") }: _*)

    val requirement = List(equal("monitor", monitor.toString), gte("time", startTime.toDate()), lt("time", endTime.toDate()), audited)

    val f = col.find(and(requirement: _*)).projection(proj).sort(ascending("time")).toFuture()
    for (docs <- f) yield {
      val timePair =
        for {
          doc <- docs
          time = doc("time").asDateTime()
        } yield {
          val mtPair =
            for {
              mt <- mtList
              mtBFName = MonitorType.BFName(mt)
              monitor = Monitor.withName(doc("monitor").asString().getValue)
              mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
              mtDoc = mtDocOpt.get.asDocument()
              v = mtDoc.get("v") if v.isDouble()
              s = mtDoc.get("s") if s.isString()
            } yield {
              mt -> Record(monitor, time, v.asDouble().doubleValue(), s.asString().getValue)
            }
          time.toDateTime() -> mtPair.toMap
        }
      timePair.toMap
    }
  }

  case class MtRecord(mtName: String, value: Double, status: String)
  case class RecordList(time: Long, mtDataList: Seq[MtRecord])

  implicit val mtRecordWrite = Json.writes[MtRecord]
  implicit val recordListWrite = Json.writes[RecordList]

  def getRecordListFuture(colName: String)(monitor: Monitor.Value, startTime: DateTime, endTime: DateTime) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent._
    import scala.concurrent.duration._

    val mtList = MonitorType.activeMtvList
    val col = MongoDB.database.getCollection(colName)
    val projFields = "time" :: mtList.map { MonitorType.BFName(_) }
    val proj = include(projFields: _*)
    val f = col.find(and(equal("monitor", monitor.toString), gte("time", startTime.toDate()), lt("time", endTime.toDate()))).projection(proj).sort(ascending("time")).toFuture()

    for {
      docs <- f
    } yield {
      for {
        doc <- docs
        time = doc("time").asDateTime()
      } yield {
        val mtDataList =
          for {
            mt <- mtList
            mtBFName = MonitorType.BFName(mt)

            mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
            mtDoc = mtDocOpt.get.asDocument()
            v = mtDoc.get("v") if v.isDouble()
            s = mtDoc.get("s") if s.isString()
          } yield {
            MtRecord(mtBFName, v.asDouble().doubleValue(), s.asString().getValue)
          }
        RecordList(time.getMillis, mtDataList)
      }
    }
  }

  def getLatestRecordMapFuture(colName: String) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent._
    import scala.concurrent.duration._

    val mtList = MonitorType.activeMtvList
    val col = MongoDB.database.getCollection(colName)
    val projFields = "monitor" :: "time" :: MonitorType.mtvList.map { MonitorType.BFName(_) }
    val proj = include(projFields: _*)
    val futureList =
      for (m <- Monitor.mvList) yield {
        val f = col.find(equal("monitor", Monitor.map(m)._id)).projection(proj).sort(descending("time")).limit(1).toFuture()
        for {
          docs <- f
        } yield {
          if (docs.isEmpty)
            None
          else {
            val doc = docs.head
            val time = doc("time").asDateTime().toDateTime()
            val pair =
              for {
                mt <- MonitorType.mtvList
                mtBFName = MonitorType.BFName(mt)
                mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
                mtDoc = mtDocOpt.get.asDocument()
                v = mtDoc.get("v") if v.isDouble()
                s = mtDoc.get("s") if s.isString()
              } yield {
                mt -> Record(m, time, v.asDouble().doubleValue(), s.asString().getValue)
              }
            Some(m -> (time, pair.toMap))
          }
        }
      }
    for (pairs <- Future.sequence(futureList)) yield {
      pairs.flatMap(x=>x).toMap
    }
  }

  def getLatestRecordMap2Future(colName: String) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model.Filters._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._

    val mtList = MonitorType.activeMtvList
    val col = MongoDB.database.getCollection(colName)
    val projFields = "monitor" :: "time" :: MonitorType.mtvList.map { MonitorType.BFName(_) }
    val targetTime = (DateTime.now() - 2.hour).withMinuteOfHour(0).withSecond(0).withMillisOfSecond(0)
    val proj = include(projFields: _*)
    val f = col.find(equal("time", targetTime.toDate())).projection(proj).toFuture()
    val pairsF =
      for {
        docs <- f
      } yield {
        for {
          doc <- docs
          m = Monitor.withName(doc("monitor").asString().getValue)
          time = doc("time").asDateTime().toDateTime()
        } yield {
          val pair =
            for {
              mt <- MonitorType.mtvList
              mtBFName = MonitorType.BFName(mt)
              mtDocOpt = doc.get(mtBFName) if mtDocOpt.isDefined && mtDocOpt.get.isDocument()
              mtDoc = mtDocOpt.get.asDocument()
              v = mtDoc.get("v") if v.isDouble()
              s = mtDoc.get("s") if s.isString()
            } yield {
              mt -> Record(m, time, v.asDouble().doubleValue(), s.asString().getValue)
            }
          m -> (time, pair.toMap)
        }
      }

    for (pairs <- pairsF) yield {
      pairs.toMap
    }
  }

  def getLatestRecordTimeFuture(colName: String, monitorList: Seq[Monitor.Value]) = {
    import org.mongodb.scala.bson._
    import org.mongodb.scala.model._
    import org.mongodb.scala.model.Projections._
    import org.mongodb.scala.model.Sorts._
    import scala.concurrent._
    import scala.concurrent.duration._

    val mtList = MonitorType.activeMtvList
    val col = MongoDB.database.getCollection(colName)
    val projFields = Seq("monitor", "time")
    val proj = include(projFields: _*)
    val futureList =
      for (m <- monitorList) yield {
        val filter = Filters.and(Filters.equal("monitor", Monitor.map(m)._id), Filters.ne("正十一烷", null))
        val f = col.find(filter).projection(proj).sort(descending("time")).limit(1).toFuture()
        for {
          docs <- f
        } yield {
          if (docs.isEmpty) {
            None
          } else {
            val doc = docs.head
            val time = doc("time").asDateTime().toDateTime()
            Some(m -> time)
          }
        }
      }
    for (pairs <- Future.sequence(futureList)) yield {
      pairs.flatMap(x => x).toMap
    }
  }
  def getWindRose(monitor: Monitor.Value, monitorType: MonitorType.Value, start: DateTime, end: DateTime, level: List[Double], nDiv: Int = 16) = {
    val windRecordFuture = getRecordListFuture(HourCollection)(monitor, start, end)
    val windRecords = waitReadyResult(windRecordFuture)

    assert(windRecords.length != 0)
    val step = 360f / nDiv
    import scala.collection.mutable.ListBuffer
    val windDirPair =
      for (d <- 0 to nDiv - 1) yield {
        (d -> ListBuffer[Double]())
      }
    val windMap = Map(windDirPair: _*)

    var total = 0
    for {
      w <- windRecords
      windDirRecOpt = w.mtDataList.find { p => p.mtName == MonitorType.map(MonitorType.WIN_DIRECTION)._id } if windDirRecOpt.isDefined
      mtRecOpt = w.mtDataList.find { p => p.mtName == MonitorType.map(monitorType)._id } if mtRecOpt.isDefined
    } {
      val mtRec = mtRecOpt.get
      val windDirRec = windDirRecOpt.get
      val dir = (Math.ceil((windDirRec.value - (step / 2)) / step).toInt) % nDiv
      windMap(dir) += mtRec.value
      total += 1
    }

    def winSpeedPercent(winSpeedList: ListBuffer[Double]) = {
      val count = new Array[Double](level.length + 1)
      def getIdx(v: Double): Int = {
        for (i <- 0 to level.length - 1) {
          if (v < level(i))
            return i
        }

        return level.length
      }

      for (w <- winSpeedList) {
        val i = getIdx(w)
        count(i) += 1
      }

      assert(total != 0)
      count.map(_ * 100 / total)
    }

    windMap.map(kv => (kv._1, winSpeedPercent(kv._2)))
  }

}