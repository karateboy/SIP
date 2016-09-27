package models
import scala.collection.Map
import play.api.Logger
import EnumUtils._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.ModelHelper._
import com.github.nscala_time.time.Imports._
import scala.concurrent.ExecutionContext.Implicits.global

case class Monitor( _id: String, indParkName: String, dp_no: String)

object Monitor extends Enumeration {
  implicit val monitorRead: Reads[Monitor.Value] = EnumUtils.enumReads(Monitor)
  implicit val monitorWrite: Writes[Monitor.Value] = EnumUtils.enumWrites
  implicit val mWrite = Json.writes[Monitor]
  implicit val mRead = Json.reads[Monitor]

  import org.mongodb.scala.bson._
  import scala.concurrent._
  import scala.concurrent.duration._

  implicit object TransformMonitor extends BsonTransformer[Monitor.Value] {
    def apply(m: Monitor.Value): BsonString = new BsonString(m.toString)
  }
  val colName = "monitors"
  val collection = MongoDB.database.getCollection(colName)

  def buildMonitor(indParkName: String, dp_no: String) = 
    Monitor(s"${indParkName}#${dp_no}" , indParkName, dp_no)
    
  def init(colNames: Seq[String]) = {
    if (!colNames.contains(colName)) {
      val f = MongoDB.database.createCollection(colName).toFuture()
      f.onFailure(errorHandler)
      f.onSuccess({
        case _: Seq[t] =>
          newMonitor(buildMonitor("台南科學園區", "南科實中測站"))
      })
      Some(f.mapTo[Unit])
    } else
      None
  }

  def toDocument(m: Monitor) = {
    val json = Json.toJson(m)
    Document(json.toString())
  }

  def toMonitor(d: Document) = {
    val ret = Json.parse(d.toJson()).validate[Monitor]

    ret.fold(error => {
      Logger.error(JsError.toJson(error).toString())
      throw new Exception(JsError.toJson(error).toString)
    },
      m =>
        m)
  }

  def newMonitor(m: Monitor) = {
    val f = collection.insertOne(toDocument(m)).toFuture()
    f.onFailure(errorHandler)
    f.onSuccess({
      case _: Seq[t] =>
        val v = Value(m._id)
        map = map + (v -> m)
        mvList = (v::mvList.reverse).reverse
    })
    f.mapTo[Unit]
  }

  private def mList: List[Monitor] =
    {
      val f = MongoDB.database.getCollection(colName).find().toFuture()
      val r = waitReadyResult(f)
      r.map { toMonitor }.toList
    }

  def refreshMonitor = {
    val list = mList
    for (m <- list) {
      try {
        Monitor.withName(m._id)
      } catch {
        case _: NoSuchElementException =>
          map = map + (Value(m._id) -> m)
      }
    }
    mvList = list.map(m => Monitor.withName(m._id))
  }

  var map: Map[Value, Monitor] = Map(mList.map { e => Value(e._id) -> e }: _*)
  var mvList = mList.map(mt => Monitor.withName(mt._id))

}