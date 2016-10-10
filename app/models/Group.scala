package models
import scala.collection.Map
import play.api.Logger
import EnumUtils._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.ModelHelper._
import com.github.nscala_time.time.Imports._
import scala.concurrent.ExecutionContext.Implicits.global

case class Group(_id: String, privilege: Privilege){
  def name = _id
}

object Group {
  implicit val mWrite = Json.writes[Group]
  implicit val mRead = Json.reads[Group]

  import org.mongodb.scala.bson._
  import scala.concurrent._
  import scala.concurrent.duration._

  val colName = "groups"
  val collection = MongoDB.database.getCollection(colName)

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

  def toDocument(g: Group) = {
    val json = Json.toJson(g)
    Document(json.toString())
  }

  def toGroup(d: Document) = {
    val ret = Json.parse(d.toJson()).validate[Group]

    ret.fold(error => {
      Logger.error(JsError.toJson(error).toString())
      throw new Exception(JsError.toJson(error).toString)
    },
      m =>
        m)
  }

  def newGroup(g: Group) = {
    val f = collection.insertOne(toDocument(g)).toFuture()
    f.onFailure(errorHandler)
    f.onSuccess({
      case _: Seq[t] =>
    })
  }

  import org.mongodb.scala.model.Filters._
  def updateGroup(g: Group) = {
    val f = collection.replaceOne(equal("_id", g._id), toDocument(g)).toFuture()
    f.onFailure(errorHandler)
    f
  }

  def getGroupList =
  {
      val f = collection.find().toFuture()
      for{r <- f} yield
        r.map { toGroup }.toList
  }

  def delGroup(_id:String)={
    val f = collection.deleteOne(equal("_id", _id)).toFuture()
    f.onFailure(errorHandler)
    f    
  }
}