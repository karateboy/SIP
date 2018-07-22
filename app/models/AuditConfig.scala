package models
import play.api._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import org.mongodb.scala.model._
import play.api.libs.json._

case class AuditConfig(
  _id:                    String,
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
  dataReadyMinRule:       DataReadyMinRule)

object AuditConfig {
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.bson.codecs.configuration.CodecRegistries.{ fromRegistries, fromProviders }
  
  val codecRegistry = fromRegistries(fromProviders(
    classOf[AuditConfig],
    classOf[MinMaxRule], classOf[CompareRule], classOf[DifferenceRule], classOf[SpikeRule],
    classOf[PersistenceRule], classOf[MonoRule], classOf[TwoHourRule],
    classOf[ThreeHourRule], classOf[FourHourRule], classOf[OverInternalStdMinRule],
    classOf[DataReadyMinRule]), DEFAULT_CODEC_REGISTRY)

  val ColName = "auditConfig"
  val collection = MongoDB.database.getCollection[AuditConfig](ColName).withCodecRegistry(codecRegistry)

  implicit val writes = Json.writes[AuditConfig]
  implicit val reads = Json.reads[AuditConfig]
  
  def defaultConfig(_id: String) = AuditConfig(
    _id,
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

  
  def init(colNames: Seq[String]) {
    if (!colNames.contains(ColName)) {
      val f = MongoDB.database.createCollection(ColName).toFuture()
      f.onFailure(errorHandler)
    }
  }
  //
  import org.mongodb.scala.model._

  def upsert(_id: String, textile: AuditConfig) = {
    val f = collection.replaceOne(Filters.eq("_id", _id), textile, UpdateOptions().upsert(true)).toFuture()
    f.onFailure {
      errorHandler
    }
    f
  }

  def getConfigMapFuture = {
    val f = collection.find().toFuture()
    f.onFailure(errorHandler)

    for {
      configs <- f
    } yield {
      val pairs =
        for (config <- configs)
          yield config._id -> config
      pairs.toMap
    }
  }
}