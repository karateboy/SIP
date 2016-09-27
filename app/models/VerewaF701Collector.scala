package models
import play.api._
import akka.actor._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import Protocol.ProtocolParam
import scala.concurrent.ExecutionContext.Implicits.global
import ModelHelper._

object VerewaF701Collector {
  case object OpenComPort
  case object ReadData

  var count = 0
  def start(id: String, protocolParam: ProtocolParam, mt: MonitorType.Value)(implicit context: ActorContext) = {
    import Protocol.ProtocolParam
    val actorName = s"F701_${mt}_${count}"
    count += 1
    val collector = context.actorOf(Props(classOf[VerewaF701Collector], id, protocolParam, mt), name = actorName)
    Logger.info(s"$actorName is created.")

    collector
  }

}

class VerewaF701Collector(id: String, protocolParam: ProtocolParam, mt: MonitorType.Value) extends Actor {
  import VerewaF701Collector._
  import scala.concurrent.duration._
  var cancelable = Akka.system.scheduler.scheduleOnce(Duration(1, SECONDS), self, OpenComPort)
  var serialOpt: Option[SerialComm] = None

  import scala.concurrent.Future
  import scala.concurrent.blocking
  import DataCollectManager._
  var collectorStatus = MonitorStatus.NormalStat
  var instrumentStatus: Byte = 0
  def checkStatus(status: Byte) {
    import Alarm._
    if ((instrumentStatus & 0x1) != (status & 0x1)) {
      if ((status & 0x1) == 1)
        log(instStr(id), Level.INFO, "standby")
      else
        log(instStr(id), Level.INFO, "concentration")
    }

    if ((instrumentStatus & 0x2) != (status & 0x2)) {
      if ((status & 0x2) == 1)
        log(instStr(id), Level.INFO, "Film measurement")
    }

    if ((instrumentStatus & 0x4) != (status & 0x4)) {
      if ((status & 0x4) == 1)
        log(instStr(id), Level.INFO, "Zero point measurement")
    }

    if ((instrumentStatus & 0x8) != (status & 0x8)) {
      if ((status & 0x8) == 1)
        log(instStr(id), Level.INFO, "Reference measurement (Reference check)")
    }

    if ((instrumentStatus & 0x80) != (status & 0x80)) {
      if ((status & 0x80) == 1)
        log(instStr(id), Level.INFO, "Measurement")
    }

    instrumentStatus = status
  }

  def checkErrorStatus(error: Byte) {
    import Alarm._
    if ((error & 0x1) != 0) {
      log(instStr(id), Level.WARN, "Volume error")
    }

    if ((error & 0x2) != 0) {
      log(instStr(id), Level.WARN, "Vacuum break")
    }

    if ((error & 0x4) != 0) {
      log(instStr(id), Level.WARN, "Volume<500 liter and 250 liter at 1/2 h sample time, respectively.")
    }

    if ((error & 0x10) != 0) {
      log(instStr(id), Level.ERR, "Volume < 25 liter")
    }

    if ((error & 0x20) != 0) {
      log(instStr(id), Level.ERR, "Change battery")
    }

    if ((error & 0x40) != 0) {
      log(instStr(id), Level.ERR, "Filter crack")
    }
  }

  def checkErrorStatus(channel: Int, error: Byte) {
    import Alarm._
    if (channel == 0)
      checkErrorStatus(error)
    else {
      val msgMap = Map(1 -> "Sampled volume: Volume sensor defective.",
        2 -> "Sampled mass: GM tube or amplifier defective.",
        3 -> "Filter adapter temperature: Temperature measurement defective.",
        4 -> "Temperature ambient air: Air pressure measurement defective (sensor not connected, parametrization).",
        5 -> "Ambient air pressure: Air pressure measurement defective (sensor not connected, parametrization)",
        6 -> "Relative humidity: Relative humidity measurement defective (sensor not connected, parametrization)",
        7 -> "Temperature sample tube: Temperature measurement erroneous (PT100 not connected)",
        8 -> "Temperature sample tube: Temperature measurement erroneous",
        9 -> "0 rate: GM tube or amplifier defective",
        10 -> "M rate: GM tube or amplifier defective")

      if ((error & 0x1) != 0) {
        if (msgMap.contains(channel)){
          //FIXME disable error alarm 
          //log(instStr(id), Level.ERR, msgMap(channel))
        }
      }
    }
  }

  def receive = {
    case OpenComPort =>
      try {
        serialOpt = Some(SerialComm.open(protocolParam.comPort.get))
        cancelable = Akka.system.scheduler.schedule(scala.concurrent.duration.Duration(3, SECONDS), Duration(3, SECONDS), self, ReadData)
      } catch {
        case ex: Exception =>
          Logger.error(ex.getMessage, ex)
          Logger.info("Reopen 1 min latter...")
          cancelable = Akka.system.scheduler.scheduleOnce(Duration(1, MINUTES), self, OpenComPort)
      }

    case ReadData =>
      Future {
        blocking {
          val cmd = HessenProtocol.dataQuery
          serialOpt.get.port.writeBytes(cmd)
          val replies = serialOpt.get.getLine2(timeout = 3)
          for (reply <- replies) {
            val measureList = HessenProtocol.decode(reply)
            for {
              ma_ch <- measureList.zipWithIndex
              measure = ma_ch._1
              channel = ma_ch._2
            } {
              if (channel == 0) {
                checkStatus(measure.status)
                //Logger.debug(s"$mt, $measure.value, $collectorStatus")
                context.parent ! ReportData(List(MonitorTypeData(mt, measure.value, collectorStatus)))
              }

              checkErrorStatus(channel, measure.error)
            }
            //Log instrument status...

          }
        }
      } onFailure errorHandler

    case SetState(id, state) =>
      Logger.info(s"SetState(${MonitorStatus.map(state).desp})")
  }

  override def postStop(): Unit = {
    cancelable.cancel()
    serialOpt map {
      serial =>
        SerialComm.close(serial)
    }

  }

}