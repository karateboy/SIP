package models

import akka.actor.{ Actor, ActorLogging, Props, ActorRef }
import javax.xml.ws.Holder
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.actor.actorRef2Scala
import scala.concurrent.ExecutionContext.Implicits.global
import org.mongodb.scala.bson._
import org.mongodb.scala.model._
import models.ModelHelper._

object OpenDataReceiver {
  val props = Props[OpenDataReceiver]
  case object GetEpaHourData

  var receiver: ActorRef = _
  def startup() = {
    receiver = Akka.system.actorOf(props, name = "openDataReceiver")
    Logger.info(s"OpenData receiver starts")
  }
}

import java.util.Date
case class HourData(
  SiteId:        String,
  SiteName:      String,
  ItemId:        String,
  ItemName:      String,
  ItemEngName:   String,
  ItemUnit:      String,
  MonitorDate:   Date,
  MonitorValues: Seq[Double])

class OpenDataReceiver extends Actor with ActorLogging {
  import OpenDataReceiver._
  import com.github.nscala_time.time.Imports._
  val timer = {
    import scala.concurrent.duration._
    Akka.system.scheduler.schedule(Duration(5, SECONDS), Duration(1, DAYS), receiver, GetEpaHourData)
  }

  import scala.xml._
  def getEpaHourData(start: DateTime, end: DateTime) {
    Logger.info(s"get EPA data start=${start.toString()} end=${end.toString()}")
    val limit = 1000
    def parser(node: Elem) = {
      import scala.xml.Node
      import scala.collection.mutable.Map
      val recordMap = Map.empty[Monitor.Value, Map[DateTime, Map[MonitorType.Value, (Double, String)]]]

      var currentDate = DateTime.now().withMillisOfDay(0)
      def filter(dataNode: Node) = {
        val monitorDateOpt = dataNode \ "MonitorDate"
        val mDate = DateTime.parse(s"${monitorDateOpt.text.trim()}", DateTimeFormat.forPattern("YYYY-MM-dd"))
        if (mDate != currentDate) {
          Logger.debug(s"current Date ${mDate.toString()}")
          currentDate = mDate
        }
        start <= mDate && mDate < end
      }

      def processData(dataNode: Node) {
        val siteName = dataNode \ "SiteName"
        val itemId = dataNode \ "ItemId"
        val monitorDateOpt = dataNode \ "MonitorDate"

        try {
          val epaMonitor = Monitor.getMonitorValueByName("環保署", "環保署" + siteName.text.trim())
          val monitorType = MonitorType.getMonitorTypeByItemID(itemId.text.trim().toInt).get
          val mDate = DateTime.parse(s"${monitorDateOpt.text.trim()}", DateTimeFormat.forPattern("YYYY-MM-dd"))

          val monitorNodeValueSeq =
            for (v <- 0 to 23) yield {
              val monitorValue = try {
                Some((dataNode \ "MonitorValue%02d".format(v)).text.trim().toDouble)
              } catch {
                case x: Throwable =>
                  None
              }
              (mDate + v.hour, monitorValue)
            }

          val timeMap = recordMap.getOrElseUpdate(epaMonitor, Map.empty[DateTime, Map[MonitorType.Value, (Double, String)]])
          for { (mDate, mtValueOpt) <- monitorNodeValueSeq } {
            val mtMap = timeMap.getOrElseUpdate(mDate, Map.empty[MonitorType.Value, (Double, String)])
            for (mtValue <- mtValueOpt)
              mtMap.put(monitorType, (mtValue, MonitorStatus.NormalStat))
          }
        } catch {
          case x: Throwable =>
            Logger.error("failed", x)
        }
      }

      val data = node \ "Data"

      val qualifiedData = data.filter(filter)

      Logger.debug(s"${data.length} qualified=${qualifiedData.length}")
      qualifiedData.map { processData }

      val updateModels =
        for {
          monitorMap <- recordMap
          monitor = monitorMap._1
          timeMaps = monitorMap._2
          dateTime <- timeMaps.keys.toList.sorted
          mtMaps = timeMaps(dateTime) if !mtMaps.isEmpty
          doc = Record.toDocument(monitor, dateTime, mtMaps.toList)
          updateList = doc.toList.map(kv => Updates.set(kv._1, kv._2)) if !updateList.isEmpty
        } yield {
          UpdateOneModel(
            Filters.eq("_id", doc("_id")),
            Updates.combine(updateList: _*), UpdateOptions().upsert(true))
        }

      if (!updateModels.isEmpty) {
        val collection = MongoDB.database.getCollection(Record.HourCollection)
        val f2 = collection.bulkWrite(updateModels.toList, BulkWriteOptions().ordered(false)).toFuture()
        f2.onFailure(errorHandler)
        waitReadyResult(f2)
      }

      qualifiedData.length
    }

    def getData(skip: Int) {
      val url = s"https://opendata.epa.gov.tw/webapi/api/rest/datastore/355000000I-000027/?format=xml&limit=${limit}&skip=${skip}&orderby=MonitorDate%20desc&token=00k8zvmeJkieHA9w13JvOw"
      Logger.debug(url)
      val retFuture =
        WS.url(url).get().map {
          response =>
            try {
              parser(response.xml)
            } catch {
              case ex: Exception =>
                Logger.error(ex.toString())
                throw ex
            }
        }
      val ret = waitReadyResult(retFuture)
      if (ret < limit) {
        SysConfig.set(SysConfig.EPA_LAST, end)
      } else
        getData(skip + limit)
    }

    getData(0)
  }

  def receive = {
    case GetEpaHourData =>
      for (begin <- SysConfig.get(SysConfig.EPA_LAST)) {
        val start = new DateTime(begin.asDateTime().getValue)
        val end = DateTime.now().withMillisOfDay(0)
        if (start < end) {
          getEpaHourData(start, end)
        }
      }

  }

  override def postStop = {
    timer.cancel()
  }

}

