package models
import play.api.libs.json._
import models.ModelHelper._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import com.github.nscala_time.time.Imports._
import org.mongodb.scala.model._
import org.mongodb.scala.bson._

object SysConfig extends Enumeration {
  val ColName = "sysConfig"
  val collection = MongoDB.database.getCollection(ColName)

  val valueKey = "value"
  val AVGHR_LAST = Value
  val AVGR_LAST = Value
  val SET_MT_ITEM_ID = Value
  val EPA_LAST = Value
  val DUST_LAST = Value

  val defaultConfig = Map(
    AVGHR_LAST -> Document(valueKey -> DateTime.parse("2017-1-1").toDate()),
    AVGR_LAST -> Document(valueKey -> DateTime.parse("2017-1-1").toDate()),
    SET_MT_ITEM_ID -> Document(valueKey -> false),
    EPA_LAST-> Document(valueKey -> DateTime.parse("2018-7-1").toDate()),
    DUST_LAST -> Document(valueKey -> DateTime.parse("2018-7-1").toDate()))

  def init(colNames: Seq[String]) {
    if (!colNames.contains(ColName)) {
      val f = MongoDB.database.createCollection(ColName).toFuture()
      f.onFailure(errorHandler)
    }

    val idSet = values map { _.toString() }
    //Clean up unused
    val f1 = collection.deleteMany(Filters.not(Filters.in("_id", idSet.toList: _*))).toFuture()
    f1.onFailure(errorHandler)
    val updateModels =
      for ((k, defaultDoc) <- defaultConfig) yield {
        UpdateOneModel(
          Filters.eq("_id", k.toString()),
          Updates.setOnInsert(valueKey, defaultDoc(valueKey)), UpdateOptions().upsert(true))
      }

    val f2 = collection.bulkWrite(updateModels.toList, BulkWriteOptions().ordered(false)).toFuture()

    import scala.concurrent._
    val f = Future.sequence(List(f1, f2))
    waitReadyResult(f)
  }

  def upsert(_id: SysConfig.Value, doc: Document) = {
    val uo = new UpdateOptions().upsert(true)
    val f = collection.replaceOne(Filters.equal("_id", _id.toString()), doc, uo).toFuture()
    f.onFailure(errorHandler)
    f
  }

  def get(_id: SysConfig.Value) = {
    val f = collection.find(Filters.eq("_id", _id.toString())).first().toFuture()
    f.onFailure(errorHandler)
    for (ret <- f) yield {
      val doc =
        if (ret.isEmpty)
          defaultConfig(_id)
        else
          ret
      doc("value")
    }
  }

  def set(_id: SysConfig.Value, v: BsonValue) = upsert(_id, Document(valueKey -> v))
}