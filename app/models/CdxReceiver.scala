package models

import akka.actor.{ Actor, ActorLogging, Props, ActorRef }
import javax.xml.ws.Holder
import com.github.nscala_time.time.Imports._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api._
import akka.actor.actorRef2Scala

object CdxReceiver {
  val props = Props[CdxReceiver]
  case object GetInBoxFiles
  case object ParseXML

  var receiver: ActorRef = _
  def startup() = {
    receiver = Akka.system.actorOf(props, name = "cdxReceiver")
  }

  def getInboxFiles = {
    receiver ! GetInBoxFiles
  }
  def parseXML = {
    receiver ! ParseXML
  }
}

class CdxReceiver extends Actor with ActorLogging {
  import CdxReceiver._
  import com.github.nscala_time.time.Imports._

  val path = current.path.getAbsolutePath + "/importEPA/"
  def receive = {
    case GetInBoxFiles =>
      try {
        getInBoxFileList("epbyljjliao", "Yx4M2KA4", "AQX_P_267")
        getInBoxFileList("epbyljjliao", "Yx4M2KA4", "AQX_P_268")
        Logger.info("GetInBoxFiles done.")
      } catch {
        case ex: Throwable =>
          Logger.error("getInBoxFileList failed", ex)
      }
    case ParseXML =>
      try {
        parseAllXml(path)(parser)
      } catch {
        case ex: Throwable =>
          Logger.error("ParseXML failed", ex)
      }
  }

  def getInBoxFileList(account: String, password: String, serviceId: String) = {
    val errMsgHolder = new Holder("")
    val resultHolder = new Holder[Integer]
    val fileListHolder = new Holder[com.wecc.cdx.ArrayOfAnyType]
    CdxWebService.service.getFileListByServiceId(account, password, "Inbox", serviceId, errMsgHolder, resultHolder, fileListHolder)
    if (resultHolder.value != 1) {
      Logger.error(s"errMsg:${errMsgHolder.value}")
      Logger.error(s"ret:${resultHolder.value.toString}")
    } else {
      val fileList = fileListHolder.value.getAnyType.asInstanceOf[java.util.ArrayList[String]]
      def getFile(fileName: String) = {

        val resultHolder = new Holder[Integer]
        val errMsgHolder = new Holder("")
        val fileBuffer = new Holder[Array[Byte]]
        CdxWebService.service.getFile(account, password, fileName, "Inbox", errMsgHolder, resultHolder, fileBuffer)
        if (resultHolder.value != 1) {
          Logger.error(s"errMsg:${errMsgHolder.value}")
          Logger.error(s"ret:${resultHolder.value.toString}")
        } else {
          import java.io._
          val content = fileBuffer.value
          val os = new FileOutputStream(s"$path$fileName")
          os.write(content)
          os.close()
        }
      }

      def removeFileFromServer(fileName: String) = {
        val resultHolder = new Holder[Integer]
        val errMsgHolder = new Holder("")
        val successHolder = new Holder[java.lang.Boolean]
        CdxWebService.service.getFileFinish(account, password, fileName, "Inbox", errMsgHolder, resultHolder, successHolder)
        if (resultHolder.value != 1) {
          Logger.error(s"errMsg:${errMsgHolder.value}")
          Logger.error(s"ret:${resultHolder.value.toString}")
        }
      }

      def backupFile(fileName: String) = {
        import java.nio.file._
        val srcPath = Paths.get(s"$path$fileName")
        val destPath = Paths.get(s"${path}backup/${fileName}")
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING)
      }

      Logger.info(s"#=${fileList.size()}")
      for (idx <- 0 to fileList.size() - 1) {
        val fileName = fileList.get(idx)
        Logger.debug(s"get ${fileList.get(idx)}")
        getFile(fileName)
        if (fileName.startsWith("AQX")) {
          val file = new java.io.File(s"$path$fileName")
          parser(file)
          backupFile(fileName)
        } else {
          import java.nio.file._
          Files.deleteIfExists(Paths.get(s"$path$fileName"))
        }
        removeFileFromServer(fileName)
      }
      Logger.info("Done")
    }
  }

  import java.io.File
  def parser(f: File) {
    import scala.xml.Node
    import scala.collection.mutable.Map
    val recordMap = Map.empty[Monitor.Value, Map[DateTime, Map[MonitorType.Value, (Double, String)]]]
    def processSTK(stk: Node) = {
      val indPark = (stk \ "IndParkName")
      val dpNo = (stk \ "DP_NO")
      val desp = (stk \ "DESP")
      val unit = stk \ "UNIT"
      val date = stk \ "M_Date"
      val time = stk \ "M_TIME"
      val value = stk \ "VAL"
      val status = stk \ "CODE2"

      try {
        if (!indPark.isEmpty && !dpNo.isEmpty && !desp.isEmpty && !unit.isEmpty &&
          !date.isEmpty && !time.isEmpty && !value.isEmpty && !status.isEmpty) {
          val mDate = DateTime.parse(s"${date.text} ${time.text}", DateTimeFormat.forPattern("YYYY-MM-dd HHmmss"))
          val monitor = Monitor.getMonitorValueByName(indPark.text.trim(), dpNo.text.trim())
          val monitorType = MonitorType.getMonitorTypeValueByName(desp.text.trim(), unit.text.trim())
          val timeMap = recordMap.getOrElseUpdate(monitor, Map.empty[DateTime, Map[MonitorType.Value, (Double, String)]])
          val mtMap = timeMap.getOrElseUpdate(mDate, Map.empty[MonitorType.Value, (Double, String)])
          val mtValue = try {
            value.text.toDouble
          } catch {
            case _: NumberFormatException =>
              0.0
          }

          mtMap.put(monitorType, (mtValue, status.text.trim))
        }
      } catch {
        case ex: Throwable =>
          Logger.info("skip Invalid record", ex)
      }
    }

    if (f.getName.startsWith("AQX_P_267")) {
      val node = xml.XML.loadFile(f)
      node match {
        case <AQX_P_267>{ stks @ _* }</AQX_P_267> =>
          stks.map { processSTK }
      }
    } else if (f.getName.startsWith("AQX_P_268")) {
      val node = xml.XML.loadFile(f)
      node match {
        case <AQX_P_268>{ stks @ _* }</AQX_P_268> =>
          stks.map { processSTK }
      }

    }

    for {
      monitorMap <- recordMap
      monitor = monitorMap._1
      timeMaps = monitorMap._2
      dateTime = timeMaps.keys.toList.sorted.last
      mtMaps = timeMaps(dateTime)
    } {
      if (!mtMaps.isEmpty)
        Record.upsertRecord(Record.toDocument(monitor, dateTime, mtMaps.toList))(Record.HourCollection)
    }
    Logger.info(s"${f.getName} finished")

  }

  def parseAllXml(dir: String)(parser: (File) => Unit) = {

    def listAllFiles = {
      import java.io.FileFilter
      new java.io.File(dir).listFiles.filter(_.getName.endsWith(".xml"))
    }

    def backupFile(fileName: String) = {
      import java.nio.file._
      val srcPath = Paths.get(s"$path$fileName")
      val destPath = Paths.get(s"${path}backup/${fileName}")
      try {
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING)
      } catch {
        case ex: Throwable =>
          Logger.error("backup failed", ex)
      }
    }

    val files = listAllFiles
    for (f <- files) {
      if (f.getName.startsWith("AQX")) {
        parser(f)
        backupFile(f.getName)
      } else {
        f.delete()
      }
    }
  }

  def getXmlStr(hour: DateTime) = {
    //    val xml = DbHelper.getXmlRecord(hour)
    //
    //    scala.xml.XML.save("temp.xml", xml, "UTF-8", true)
    //
    //    scala.io.Source.fromFile("temp.xml")("UTF-8").mkString
  }

  def upload(hour: DateTime, serviceId: String, user: String, password: String) = {
    val xmlStr = getXmlStr(hour)
    val fileName = s"${serviceId}_${hour.toString("MMdd")}${hour.getHourOfDay}_${user}.xml"
    val errMsgHolder = new Holder("")
    val resultHolder = new Holder(Integer.valueOf(0))
    val unknownHolder = new Holder(new java.lang.Boolean(true))
    //CdxWebService.service.putFile(user, password, fileName, xmlStr.getBytes("UTF-8"), errMsgHolder, resultHolder, unknownHolder)
    if (resultHolder.value != 1) {
      log.error(s"errMsg:${errMsgHolder.value}")
      log.error(s"ret:${resultHolder.value.toString}")
      log.error(s"unknown:${unknownHolder.value.toString}")
    } else {
      log.info(s"Success upload ${hour.toString}")
    }
  }
}