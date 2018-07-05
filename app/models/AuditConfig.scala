package models
import play.api._
import com.github.nscala_time.time.Imports._
import models.ModelHelper._
import models.ExcelTool._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import play.api.Play.current
import org.apache.poi.openxml4j.opc._
import org.apache.poi.xssf.usermodel._
import org.apache.poi.ss.usermodel._
import java.util.Date
import org.mongodb.scala.model._
import org.mongodb.scala.model.Indexes._

case class AuditConfig(
  _id:                    String,
  minMaxRule:             Option[MinMaxRule],
  compareRule:            Option[CompareRule],
  differenceRule:         Option[DifferenceRule],
  spikeRule:              Option[SpikeRule],
  persistenceRule:        Option[PersistenceRule],
  monoRule:               Option[MonoRule],
  twoHourRule:            Option[TwoHourRule],
  threeHourRule:          Option[ThreeHourRule],
  fourHourRule:           Option[FourHourRule],
  overInternalStdMinRule: Option[OverInternalStdMinRule],
  dataReadyMinRule:       Option[DataReadyMinRule])

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

  def defaultConfig(_id: String) = AuditConfig(
    _id,
    Some(MinMaxRule.default),
    Some(CompareRule.default),
    Some(DifferenceRule.default),
    Some(SpikeRule.default),
    Some(PersistenceRule.default),
    Some(MonoRule.default),
    Some(TwoHourRule.default),
    Some(ThreeHourRule.default),
    Some(FourHourRule.default),
    Some(OverInternalStdMinRule.default),
    Some(DataReadyMinRule.default))

  
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