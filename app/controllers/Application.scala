package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import Highchart._
import models._
import ModelHelper._

object Application extends Controller {

  val title = "特殊性工業區監測系統"

  def newUser = Security.Authenticated.async(BodyParsers.parse.json) {
    implicit request =>
      adminOnly({
        val newUserParam = request.body.validate[User]

        newUserParam.fold(
          error => {
            Logger.error(JsError.toJson(error).toString())
            Future { BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString())) }
          },
          param => {
            val f = User.newUser(param)
            val requestF =
              for (result <- f) yield {
                Ok(Json.obj("ok" -> true))
              }

            requestF.recover({
              case _: Throwable =>
                Logger.info("recover from newUser error...")
                Ok(Json.obj("ok" -> false))
            })
          })
      })
  }

  def deleteUser(email: String) = Security.Authenticated.async {
    implicit request =>
      adminOnly({
        val f = User.deleteUser(email)
        val requestF =
          for (result <- f) yield {
            Ok(Json.obj("ok" -> (result.getDeletedCount == 1)))
          }

        requestF.recover({
          case _: Throwable =>
            Logger.info("recover from deleteUser error...")
            Ok(Json.obj("ok" -> false))
        })
      })
  }

  def updateUser(id: String) = Security.Authenticated.async(BodyParsers.parse.json) {
    implicit request =>
      val userParam = request.body.validate[User]

      userParam.fold(
        error => {
          Future {
            Logger.error(JsError.toJson(error).toString())
            BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
          }
        },
        param => {
          val f = User.updateUser(param)
          for (ret <- f) yield {
            Ok(Json.obj("ok" -> (ret.getMatchedCount == 1)))
          }
        })
  }

  def getAllUsers = Security.Authenticated.async {
    val userF = User.getAllUsersFuture()
    for (users <- userF) yield Ok(Json.toJson(users))
  }

  def adminOnly[A, B <: controllers.Security.UserInfo](permited: Future[Result])(implicit request: play.api.mvc.Security.AuthenticatedRequest[A, B]) = {
    val userInfoOpt = Security.getUserinfo(request)
    if (userInfoOpt.isEmpty)
      Future {
        Forbidden("No such user!")
      }
    else {
      val userInfo = userInfoOpt.get
      val userF = User.getUserByIdFuture(userInfo.id)
      val userOpt = waitReadyResult(userF)
      if (userOpt.isEmpty || userOpt.get.groupId != Group.adminID)
        Future {
          Forbidden("無權限!")
        }
      else {
        permited
      }
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  def getGroupInfoList = Action {
    val infoList = Group.getInfoList
    implicit val write = Json.writes[GroupInfo]
    Ok(Json.toJson(infoList))
  }

  val path = current.path.getAbsolutePath + "/importEPA/"

  def importEpa103 = Action {
    Epa103Importer.importData(path)
    Ok(s"匯入 $path")
  }

  def parseXML = Security.Authenticated {
    CdxReceiver.parseXML
    Ok(s"parse XML $path")
  }

  def monitorConfig = Security.Authenticated {
    Ok(views.html.monitorConfig())
  }

  def monitorTypeConfig = Security.Authenticated {
    implicit request =>
      Ok(views.html.monitorTypeConfig())
  }

  case class EditData(id: String, data: String)
  def saveMonitorTypeConfig() = Security.Authenticated {
    implicit request =>
      try {
        val mtForm = Form(
          mapping(
            "id" -> text,
            "data" -> text)(EditData.apply)(EditData.unapply))

        val mtData = mtForm.bindFromRequest.get
        val mtInfo = mtData.id.split(":")
        val mt = MonitorType.withName(mtInfo(0))

        MonitorType.updateMonitorType(mt, mtInfo(1), mtData.data)

        Ok(mtData.data)
      } catch {
        case ex: Throwable =>
          Logger.error(ex.getMessage, ex)
          BadRequest(ex.toString)
      }
  }

  def saveMonitorConfig() = Security.Authenticated {
    implicit request =>
      try {
        val mtForm = Form(
          mapping(
            "id" -> text,
            "data" -> text)(EditData.apply)(EditData.unapply))

        val mtData = mtForm.bindFromRequest.get
        val mtInfo = mtData.id.split(":")
        val m = Monitor.withName(mtInfo(0))

        Monitor.updateMonitor(m, mtInfo(1), mtData.data)

        Ok(mtData.data)
      } catch {
        case ex: Throwable =>
          Logger.error(ex.getMessage, ex)
          BadRequest(ex.toString)
      }
  }

  def monitorTypeList = Security.Authenticated.async {
    implicit request =>
      val userOptF = User.getUserByIdFuture(request.user.id)
      for {
        userOpt <- userOptF if userOpt.isDefined
        groupInfo = Group.getGroupInfo(userOpt.get.groupId)
      } yield {
        val mtList = groupInfo.privilege.allowedMonitorTypes.map { MonitorType.map }

        Ok(Json.toJson(mtList))
      }
  }

  def monitorList = Security.Authenticated.async {
    implicit request =>
      val userOptF = User.getUserByIdFuture(request.user.id)
      for {
        userOpt <- userOptF if userOpt.isDefined
        groupInfo = Group.getGroupInfo(userOpt.get.groupId)
      } yield {

        val mList =
          groupInfo.privilege.allowedMonitors.map { Monitor.map }
        Ok(Json.toJson(mList))
      }
  }

  def indParkList = Security.Authenticated.async {
    implicit request =>
      val userOptF = User.getUserByIdFuture(request.user.id)
      for {
        userOpt <- userOptF if userOpt.isDefined
        groupInfo = Group.getGroupInfo(userOpt.get.groupId)
      } yield {

        val indParks =
          groupInfo.privilege.allowedIndParks

        Ok(Json.toJson(indParks))
      }
  }

  def reportUnitList = Security.Authenticated {
    implicit val ruWrite = Json.writes[ReportUnit]
    Ok(Json.toJson(ReportUnit.values.toList.sorted.map { ReportUnit.map }))
  }

  def upsertMonitorType(id: String) = Security.Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val mtResult = request.body.validate[MonitorType]

      mtResult.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
        },
        mt => {
          MonitorType.upsertMonitorType(mt)
          MonitorType.refreshMtv
          Ok(Json.obj("ok" -> true))
        })
  }

  def upsertMonitor(id: String) = Security.Authenticated.async(BodyParsers.parse.json) {
    implicit request =>
      val mtResult = request.body.validate[Monitor]

      mtResult.fold(
        error => {
          Future {
            Logger.error(JsError.toJson(error).toString())
            BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
          }
        },
        monitor => {
          for (ret <- Monitor.upsert(monitor)) yield {
            Monitor.refresh
            Ok(Json.obj("ok" -> true))
          }
        })
  }
  def dataManagement = Security.Authenticated {
    Ok(views.html.dataManagement())
  }

  def recalculateHour(startStr: String, endStr: String) = Security.Authenticated {
    val start = DateTime.parse(startStr, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))
    val end = DateTime.parse(endStr, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))

    for (hour <- Query.getPeriods(start, end, 1.hour)) {
      DataCollectManager.recalculateHourData(hour, false)(MonitorType.mtvList)
    }
    Ok(Json.obj("ok" -> true))
  }

  def auditConfig = Security.Authenticated {
    Ok(views.html.auditConfig())
  }

  def getAllMonitorAuditConfig = Security.Authenticated.async {
    implicit request =>
      implicit val configWrite = Json.writes[AuditConfig]
      val mapF = AuditConfig.getConfigMapFuture
      val userOptF = User.getUserByIdFuture(request.user.id)
      for {
        map <- mapF
        userOpt <- userOptF if userOpt.isDefined
        groupInfo = Group.getGroupInfo(userOpt.get.groupId)
        mList = groupInfo.privilege.allowedMonitors.map { Monitor.map }
      } yield {
        var fullMap = map
        for (m <- mList) {
          if (!fullMap.contains(m._id))
            fullMap += m._id -> AuditConfig.defaultConfig(m._id)
        }
        Ok(Json.toJson(fullMap))
      }
  }

  def getMonitorAuditConfig(rawMonitorStr: String) = Security.Authenticated {
    implicit request =>
      ???
    /*
      val monitorStr = java.net.URLDecoder.decode(rawMonitorStr, "UTF-8")
      val m = Monitor.withName(monitorStr)

      val autoAudit = Monitor.map(m).autoAudit.getOrElse(AutoAudit.default)
			*/
    //Ok(Json.toJson(autoAudit))
  }

  def setMonitorAuditConfig(rawMonitorStr: String) = Security.Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val monitorStr = java.net.URLDecoder.decode(rawMonitorStr, "UTF-8")
      val monitor = Monitor.withName(monitorStr)
      val autoAuditResult = request.body.validate[AutoAudit]

      autoAuditResult.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
        },
        autoAudit => {
          Monitor.updateMonitorAutoAudit(monitor, autoAudit)
          Ok(Json.obj("ok" -> true))
        })
  }

  def menuRightList = Security.Authenticated.async {
    implicit request =>
      val userOptF = User.getUserByIdFuture(request.user.id)
      for {
        userOpt <- userOptF if userOpt.isDefined
        groupInfo = Group.getGroupInfo(userOpt.get.groupId)
      } yield {
        val menuRightList =
          groupInfo.privilege.allowedMenuRights.map { v => MenuRight(v, MenuRight.map(v)) }

        Ok(Json.toJson(menuRightList))
      }
  }

  case class SipRecord(monitorId: String, monitorTypeId: String, time: Long, value: Double, status: String)
  case class SipCalibration(monitorId: String, monitorTypeId: String, startTime: Long, endTime: Long,
                            span: Double, zero_std: Double, zero_val: Double, span_std: Double, span_val: Double)

  implicit val sipRecordReads = Json.reads[SipRecord]
  implicit val sipCalibrationReads = Json.reads[SipCalibration]

  def receiveHourData = receiveMonitorData(Record.HourCollection)
  def receiveMinData = receiveMonitorData(Record.MinCollection)

  def receiveMonitorData(collectionName: String) = Action.async(BodyParsers.parse.json) {
    implicit request =>
      val recordsResult = request.body.validate[Seq[SipRecord]]

      recordsResult.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          Future { BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString())) }
        },
        records => {
          import scala.collection.mutable.Map
          def checkRecordMap(recordMap: Map[Monitor.Value, Map[DateTime, Map[MonitorType.Value, (Double, String)]]]) = {
            for {
              monitorMap <- recordMap
              monitor = monitorMap._1
              timeMaps = monitorMap._2
              dateTime <- timeMaps.keys.toList.sorted
              mtMaps = timeMaps(dateTime) if (!mtMaps.isEmpty)
            } {
              for (mt <- mtMaps.keys) {
                val mtCase = MonitorType.map(mt)
                var alarmed = false
                if (mtCase.std_law.isDefined) {
                  val record = mtMaps(mt)
                  if (MonitorStatusFilter.isMatched(MonitorStatusFilter.ValidData, record._2)) {
                    if (record._1 >= mtCase.std_law.get) {
                      Alarm.log(monitor, mt,
                        s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mt, Some(record._1))}超過超高警報值${MonitorType.format(mt, mtCase.std_law)}")
                      alarmed = true
                    }
                  }
                }

                if (mtCase.std_internal.isDefined && !alarmed) {
                  val record = mtMaps(mt)
                  if (MonitorStatusFilter.isMatched(MonitorStatusFilter.ValidData, record._2)) {
                    if (record._1 >= mtCase.std_internal.get) {
                      Alarm.log(monitor, mt, s"${dateTime.toString("YYYY-MM-dd HH:mm")}測值${MonitorType.format(mt, Some(record._1))}超過警報值${MonitorType.format(mt, mtCase.std_internal)}")
                    }
                  }
                }
              }
            }
          }

          val recordMap = Map.empty[Monitor.Value, Map[DateTime, Map[MonitorType.Value, (Double, String)]]]
          for (record <- records) {
            try {
              val mDate = new DateTime(record.time)
              val monitor = Monitor.withName(record.monitorId)
              val monitorType = MonitorType.getMonitorTypeValueByName(record.monitorTypeId, "ppb")
              val timeMap = recordMap.getOrElseUpdate(monitor, Map.empty[DateTime, Map[MonitorType.Value, (Double, String)]])
              val mtMap = timeMap.getOrElseUpdate(mDate, Map.empty[MonitorType.Value, (Double, String)])
              mtMap.put(monitorType, (record.value, record.status))
            } catch {
              case ex: Throwable =>
                Logger.error("skip invalid record ", ex)
            }
          }

          val f =
            for {
              monitorMap <- recordMap
              monitor = monitorMap._1
              timeMaps = monitorMap._2
              dateTime <- timeMaps.keys.toList.sorted
              mtMaps = timeMaps(dateTime) if (!mtMaps.isEmpty)
            } yield {
              Record.upsertRecord(Record.toDocument(monitor, dateTime, mtMaps.toList))(collectionName)
            }

          val retF = Future.sequence(f.toList)

          if (collectionName == Record.HourCollection) {
            checkRecordMap(recordMap)
            AutoAudit.audit(recordMap, true)
          }

          val requestF =
            for (result <- retF) yield {
              Ok(Json.obj("Ok" -> true))
            }

          requestF.recover({
            case _: Throwable =>
              Logger.info("recover from upsert data error...")
              Ok(Json.obj("Ok" -> false))
          })
        })

  }

  def receiveCalibration = Action.async(BodyParsers.parse.json) {
    implicit request =>
      val recordsResult = request.body.validate[Seq[SipCalibration]]

      recordsResult.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          Future { BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString())) }
        },
        records => {
          import scala.collection.mutable.Map
          val calibrations =
            for (record <- records) yield {
              val startTime = new DateTime(record.startTime)
              val endTime = new DateTime(record.endTime)
              val monitor = Monitor.withName(record.monitorId)
              val monitorType = MonitorType.getMonitorTypeValueByName(record.monitorTypeId, "ppb")

              Calibration(monitor, monitorType, startTime, endTime,
                Some(record.span), Some(record.zero_std), Some(record.zero_val), Some(record.span_std),
                Some(record.span_val))
            }

          val retF = Calibration.insert(calibrations)
          val requestF =
            for (result <- retF) yield {
              Ok(Json.obj("Ok" -> true))
            }

          requestF.recover({
            case _: Throwable =>
              Logger.info("recover from upsert hour error...")
              Ok(Json.obj("Ok" -> false))
          })
        })
  }

  def testSMS(mobile: String) = Security.Authenticated {
    Every8d.sendSMS("測試", "測試警報", List(mobile))
    Ok("")
  }

  def testAlarm = Security.Authenticated {
    Alarm.log(Monitor.withName("台塑六輕工業園區#彰化縣大城站"), MonitorType.withName("PM10"), "測試警報")
    Ok("")
  }
  
  def defaultAuditConfig = Security.Authenticated {
    AuditConfig.defaultConfig("default")
    Ok(Json.toJson(AuditConfig.defaultConfig("default")))
  }
}
