package models
import play.api._
import ModelHelper._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import com.typesafe.config.ConfigFactory

case class Baseline9000Config(calibrationTime: LocalTime, 
                              raiseTime: Int, downTime: Int, holdTime: Int, calibrateZeoSeq:Option[Int], calibrateSpanSeq:Option[Int])

object Baseline9000 extends DriverOps {
  implicit val cfgRead = Json.reads[Baseline9000Config]
  implicit val cfgWrite = Json.writes[Baseline9000Config]

  override def verifyParam(json: String) = {
    val ret = Json.parse(json).validate[Baseline9000Config]
    ret.fold(
      error => {
        Logger.error(JsError.toJson(error).toString())
        throw new Exception(JsError.toJson(error).toString())
      },
      param => {
        Json.toJson(param).toString()
      })
  }

  override def getMonitorTypes(param: String): List[MonitorType.Value] = {
    List(MonitorType.withName("CH4"), MonitorType.withName("NMHC"), MonitorType.withName("THC"))
  }

  def validateParam(json: String) = {
    val ret = Json.parse(json).validate[Baseline9000Config]
    ret.fold(
      error => {
        Logger.error(JsError.toJson(error).toString())
        throw new Exception(JsError.toJson(error).toString())
      },
      param => param)
  }

  override def getCalibrationTime(param: String) = {
    val config = validateParam(param)
    Some(config.calibrationTime)
  }

  import Protocol.ProtocolParam
  import akka.actor._

  def start(id:String, protocol:ProtocolParam, param:String)(implicit context:ActorContext):ActorRef={
    val config = validateParam(param)

    Baseline9000Collector.start(id, protocol, config)
  }

}