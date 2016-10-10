package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
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

object Application extends Controller {

  val title = "特殊性工業區監測系統"

  def index = Security.Authenticated {
    implicit request =>
      val user = request.user
      Ok(views.html.outline(title, user, views.html.dashboard("test")))
  }

  def dashboard = Security.Authenticated {
    Ok(views.html.dashboard(""))
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

  def userManagement() = Security.Authenticated {
    implicit request =>
      val userInfoOpt = Security.getUserinfo(request)
      if (userInfoOpt.isEmpty)
        Forbidden("No such user!")
      else {
        val userInfo = userInfoOpt.get
        val user = User.getUserByEmail(userInfo.id).get
        val userList =
          if (!user.isAdmin)
            List.empty[User]
          else
            User.getAllUsers.toList

        Ok(views.html.userManagement(userInfo, user, userList))
      }
  }

  import models.User._
  implicit val userParamRead: Reads[User] = Json.reads[User]

  def newUser = Security.Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val newUserParam = request.body.validate[User]

      newUserParam.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
        },
        param => {
          User.newUser(param)
          Ok(Json.obj("ok" -> true))
        })
  }

  def deleteUser(email: String) = Security.Authenticated {
    implicit request =>
      val userInfoOpt = Security.getUserinfo(request)
      val userInfo = userInfoOpt.get

      User.deleteUser(email)
      Ok(Json.obj("ok" -> true))
  }

  def updateUser(id: String) = Security.Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val userParam = request.body.validate[User]

      userParam.fold(
        error => {
          Logger.error(JsError.toJson(error).toString())
          BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
        },
        param => {
          User.updateUser(param)
          Ok(Json.obj("ok" -> true))
        })
  }

  def getAllUsers = Security.Authenticated {
    val users = User.getAllUsers()
    implicit val userWrites = Json.writes[User]

    Ok(Json.toJson(users))
  }

  def groupManagement() = Security.Authenticated {
    implicit request =>
      val userInfoOpt = Security.getUserinfo(request)
      if (userInfoOpt.isEmpty)
        Forbidden("No such user!")
      else {
        val userInfo = userInfoOpt.get
        val user = User.getUserByEmail(userInfo.id).get
        if (!user.isAdmin)
          Forbidden("無權限!")
        else
          Ok(views.html.groupManagement(userInfo))
      }
  }

  def newGroup = Security.Authenticated(BodyParsers.parse.json) {
    implicit request =>
      val userInfoOpt = Security.getUserinfo(request)
      if (userInfoOpt.isEmpty)
        Forbidden("No such user!")
      else {
        val userInfo = userInfoOpt.get
        val user = User.getUserByEmail(userInfo.id).get
        if (!user.isAdmin)
          Forbidden("無權限!")
        else {
          val groupResult = request.body.validate[Group]

          groupResult.fold(error => {
            Logger.error(JsError.toJson(error).toString())
            BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
          },
            group => {
              Group.newGroup(group)
              Ok(Json.obj("ok" -> true))
            })
        }
      }
  }
  
  import scala.concurrent.ExecutionContext.Implicits.global
  def getAllGroups = Security.Authenticated.async {
      val f = Group.getGroupList
      for(groupList <- f)yield{
        Ok(Json.toJson(groupList))
      }
  }
  
  def deleteGroup(id:String) = Security.Authenticated.async{
    val f = Group.delGroup(id)
    for(ret <- f) yield{
      Ok(Json.obj("ok" -> true))
    }
  }
  
  def updateGroup(id:String) = Security.Authenticated(BodyParsers.parse.json){
    implicit request =>
      val userInfoOpt = Security.getUserinfo(request)
      if (userInfoOpt.isEmpty)
        Forbidden("No such user!")
      else {
        val userInfo = userInfoOpt.get
        val user = User.getUserByEmail(userInfo.id).get
        if (!user.isAdmin)
          Forbidden("無權限!")
        else {
          val groupResult = request.body.validate[Group]

          groupResult.fold(error => {
            Logger.error(JsError.toJson(error).toString())
            BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
          },
            group => {
              Group.newGroup(group)
              Ok(Json.obj("ok" -> true))
            })
        }
      }
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

  def monitorTypeList = Security.Authenticated {
    val mtList = MonitorType.mtvList.map { mt => MonitorType.map(mt) }
    Ok(Json.toJson(mtList))
  }

  def monitorList = Security.Authenticated {
    val mList = Monitor.mvList map { Monitor.map }
    Ok(Json.toJson(mList))
  }

  def indParkList = Security.Authenticated {
    Ok(Json.toJson(Monitor.indParkSet))
  }

  def reportUnitList = Security.Authenticated {
    implicit val ruWrite = Json.writes[ReportUnit]
    Ok(Json.toJson(ReportUnit.values.map { ReportUnit.map }))
  }

  def upsertMonitorType(id: String) = Security.Authenticated(BodyParsers.parse.json) {
    Logger.info(s"upsert Mt:$id")
    implicit request =>
      val mtResult = request.body.validate[MonitorType]

      mtResult.fold(error => {
        Logger.error(JsError.toJson(error).toString())
        BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error).toString()))
      },
        mt => {
          MonitorType.upsertMonitorType(mt)
          Ok(Json.obj("ok" -> true))
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
}
