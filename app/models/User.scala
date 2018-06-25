package models
import play.api._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import models._
import org.mongodb.scala.bson.Document
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

case class User(_id: String, password: String, name: String, phone: String, groupId: String = Group.adminID, 
    alarm:Option[Boolean] = Some(true)){
}

object User {
  import scala.concurrent._
  import scala.concurrent.duration._

  val ColName = "users"
  val collection = MongoDB.database.getCollection(ColName)
  implicit val userRead = Json.reads[User]
  implicit val userWrite = Json.writes[User]
  
  def toDocument(user: User) = Document(Json.toJson(user).toString())

  def init(colNames: Seq[String]) {
    if (!colNames.contains(ColName)) {
      val f = MongoDB.database.createCollection(ColName).toFuture()
      f.onFailure(errorHandler)
    }
    val f = collection.count().toFuture()
    f.onSuccess({
      case count =>
        if (count == 0) {
          val defaultUser = User("karateboy", "abc123", "karateboy", "0920660136")
          Logger.info("Create default user:" + defaultUser.toString())
          newUser(defaultUser)
        }
    })
    f.onFailure(errorHandler)
  }

  def toUser(doc: Document) = {
    val ret = Json.parse(doc.toJson()).validate[User]

    ret.fold(error => {
      Logger.error(JsError.toJson(error).toString())
      throw new Exception(JsError.toJson(error).toString)
    },
      usr => usr)

  }

  def newUser(user: User) = {
    collection.insertOne(toDocument(user)).toFuture()
  }

  import org.mongodb.scala.model.Filters._
  def deleteUser(email: String) = {
    collection.deleteOne(equal("_id", email)).toFuture()
  }

  def updateUser(user: User) = {
    val f = collection.replaceOne(equal("_id", user._id), toDocument(user)).toFuture()
    f
  }

  def getUserByIdFuture(_id: String) = {
    val f = collection.find(equal("_id", _id)).limit(1).toFuture()
    f.onFailure { errorHandler }
    for (ret <- f)
      yield if (ret.length == 0)
      None
    else
      Some(toUser(ret(0)))
  }

  def getAllUsersFuture() = {
    val f = collection.find().toFuture()
    f.onFailure { errorHandler }
    for (ret <- f) yield ret.map { toUser }
  }
}
