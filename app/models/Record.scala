package models
import play.api._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import models._
import scala.concurrent.ExecutionContext.Implicits.global
import org.mongodb.scala._

case class Record(monitor: Monitor.Value, time: DateTime, value: Double, status: String)

object Record {
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
    val proj = include(mtList.map { MonitorType.BFName(_) }: _*)
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

  def getLatestRecordMapFuture(colName: String)(startTime: DateTime) = {
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
    val f = col.find(equal("time", startTime.toDate())).projection(proj).sort(ascending("time")).toFuture()

    for {
      docs <- f
    } yield {
      val mtPair =
        for {
          doc <- docs
          monitor = Monitor.withName(doc("monitor").asString().getValue)
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
              mt -> Record(monitor, startTime, v.asDouble().doubleValue(), s.asString().getValue)
            }
          monitor -> (pair.toMap)
        }
      mtPair.toMap
    }
  }
}