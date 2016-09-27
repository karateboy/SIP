package com.wecc

import akka.actor.{ Actor, ActorLogging, Props, ActorRef }
import javax.xml.ws.Holder
import com.github.nscala_time.time.Imports._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api._

object CdxReceiver {
  val props = Props[CdxReceiver]
  case object GetInBoxFileList

  var receiver: ActorRef = _
  def startup() = {
    receiver = Akka.system.actorOf(props, name = "cdxReceiver")

    receiver ! GetInBoxFileList
  }
}

class CdxReceiver extends Actor with ActorLogging {
  import CdxReceiver._
  import com.github.nscala_time.time.Imports._

  def receive = {
    case GetInBoxFileList =>
      try {
        Logger.info("GetInBoxFileList")
        getInBoxFileList("epbyljjliao", "Yx4M2KA4", "AQX_P_267")
      } catch {
        case ex: Throwable =>
          Logger.error("getInBoxFileList failed", ex)
      }
  }

  def getInBoxFileList(account: String, password: String, serviceId: String) = {
    val errMsgHolder = new Holder("")
    val resultHolder = new Holder[Integer]
    val fileListHolder = new Holder[cdx.ArrayOfAnyType]
    CdxWebService.service.getFileListByServiceId(account, password, "Inbox", serviceId, errMsgHolder, resultHolder, fileListHolder)
    if (resultHolder.value != 1) {
      Logger.error(s"errMsg:${errMsgHolder.value}")
      Logger.error(s"ret:${resultHolder.value.toString}")
    } else {
      val fileList = fileListHolder.value.getAnyType.asInstanceOf[java.util.ArrayList[String]]
      def getFile(fileName: String) = {
        val path = current.path.getAbsolutePath + "/importEPA/"

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

      Logger.info(s"#=${fileList.size()}")
      for (idx <- 0 to fileList.size() - 1) {
        getFile(fileList.get(idx))
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