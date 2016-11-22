package models
import scala.collection.Map
import play.api.Logger
import EnumUtils._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.ModelHelper._
import com.github.nscala_time.time.Imports._
import scala.concurrent.ExecutionContext.Implicits.global

case class MonitorV1(_id: String, indParkName: String, dp_no: String) {
  def upgrade = Monitor(_id, indParkName, dp_no, Seq.empty[MonitorType.Value])
}

case class Monitor(_id: String, indParkName: String, dp_no: String,
                   monitorTypes: Seq[MonitorType.Value] = Seq.empty[MonitorType.Value])

object Monitor extends Enumeration {
  implicit val monitorRead: Reads[Monitor.Value] = EnumUtils.enumReads(Monitor)
  implicit val monitorWrite: Writes[Monitor.Value] = EnumUtils.enumWrites
  implicit val mWrite = Json.writes[Monitor]
  implicit val m1Read = Json.reads[MonitorV1]
  implicit val mRead = Json.reads[Monitor]

  import org.mongodb.scala.bson._
  import scala.concurrent._
  import scala.concurrent.duration._

  implicit object TransformMonitor extends BsonTransformer[Monitor.Value] {
    def apply(m: Monitor.Value): BsonString = new BsonString(m.toString)
  }
  val colName = "monitors"
  val collection = MongoDB.database.getCollection(colName)

  def monitorId(indParkName: String, dp_no: String) = s"${indParkName}#${dp_no}"

  def buildMonitor(indParkName: String, dp_no: String) = {
    assert(!indParkName.isEmpty)
    assert(!dp_no.isEmpty)

    Monitor(monitorId(indParkName, dp_no), indParkName, dp_no)
  }

  def init(colNames: Seq[String]) = {
    if (!colNames.contains(colName)) {
      val f = MongoDB.database.createCollection(colName).toFuture()
      f.onFailure(errorHandler)
      f.onSuccess({
        case _: Seq[t] =>

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
      //Try v1
      val ret1 = Json.parse(d.toJson()).validate[MonitorV1]
      ret1.fold(error => {
        Logger.error(JsError.toJson(error).toString())
        throw new Exception(JsError.toJson(error).toString)
      },
        mv1 =>
          mv1.upgrade)
    },
      m => m)
  }

  def newMonitor(m: Monitor) = {
    Logger.debug(s"Create monitor value ${m._id}!")
    val v = Value(m._id)
    map = map + (v -> m)
    mvList = (v :: mvList.reverse).reverse

    val f = collection.insertOne(toDocument(m)).toFuture()
    f.onFailure(errorHandler)
    f.onSuccess({
      case _: Seq[t] =>
    })
    Monitor.withName(m._id)
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
  def indParkSet = mvList.map { map(_).indParkName }.foldRight(Set.empty[String])((name, set) => set + name)

  def getMonitorValueByName(indParkName: String, dp_no: String) = {
    try {
      val id = monitorId(indParkName, dp_no)
      Monitor.withName(id)
    } catch {
      case _: NoSuchElementException =>
        newMonitor(buildMonitor(indParkName, dp_no))
    }
  }
}