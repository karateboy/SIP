package models
import play.api._
import play.api.mvc._
import play.api.Logger
import models.ModelHelper._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.implicitConversions

case class MenuRight(id: MenuRight.Value, desp: String)
object MenuRight extends Enumeration {
  implicit val mReads: Reads[MenuRight.Value] = EnumUtils.enumReads(MenuRight)
  implicit val mWrites: Writes[MenuRight.Value] = EnumUtils.enumWrites
  implicit val mrWrites = Json.writes[MenuRight]

  val RealtimeInfo = Value("RealtimeInfo")
  val DataQuery = Value("DataQuery")
  val Report = Value("Report")
  val SystemManagement = Value("SystemManagement")

  val map = Map(
    RealtimeInfo -> "即時資訊",
    DataQuery -> "數據查詢",
    Report -> "報表查詢",
    SystemManagement -> "系統管理")
  def getDisplayName(v: MenuRight.Value) = {
    map(v)
  }
}
case class MonitorFilter(indPark: String, names: Seq[String])
case class Privilege(
  allowedIndParks:     Seq[String],
  allowedMonitorTypes: Seq[MonitorType.Value],
  allowedMenuRights:   Seq[MenuRight.Value],
  monitorFilters:      Seq[MonitorFilter]) {
  def allowedMonitors(): Seq[Monitor.Value] = {
    monitorFilters flatMap { filter =>
      Monitor.indParkMonitor(filter.indPark) filter {
        mv =>
          filter.names.isEmpty ||
            filter.names.exists({ name => Monitor.map(mv).dp_no.contains(name) })
      }
    }
  }
}

import scala.concurrent.ExecutionContext.Implicits.global
object Privilege {
  implicit val mfWrite = Json.writes[MonitorFilter]
  implicit val mfRead = Json.reads[MonitorFilter]
  implicit val privilegeWrite = Json.writes[Privilege]
  implicit val privilegeRead = Json.reads[Privilege]

  val defaultMonitorFilters = Seq(
    MonitorFilter("台塑六輕工業園區", Seq.empty[String]),
    MonitorFilter("環保署", Seq.empty[String]),
    MonitorFilter("揚塵測站", Seq("旭光", "義賢")))

  lazy val defaultPrivilege = Privilege(Monitor.indParkSet.toSeq, MonitorType.values.toSeq, MenuRight.values.toSeq, defaultMonitorFilters)
  val emptyPrivilege = Privilege(Seq.empty[String], Seq.empty[MonitorType.Value], Seq.empty[MenuRight.Value], defaultMonitorFilters)

  def myMonitorList(email: String) = {
    for {
      userOpt <- User.getUserByIdFuture(email)
      groupInfo = Group.map(userOpt.get.groupId)
    } yield {
      groupInfo.privilege.allowedMonitors.filter { m =>
        val indParkName = Monitor.map(m).indParkName
        groupInfo.privilege.allowedIndParks.contains(indParkName)
      }

    }

  }
}