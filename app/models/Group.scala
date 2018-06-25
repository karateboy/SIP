package models

case class GroupInfo(id: String, name: String, privilege: Privilege)
object Group extends Enumeration {

  def mapEntry(_id: String, name: String, privilege: Privilege) =
    _id -> GroupInfo(_id.toString, name, privilege)

  val adminID = "admin"
  val adminGroup = mapEntry(adminID, "系統管理員", Privilege.defaultPrivilege)
  
  val map = Map(
    adminGroup,
    mapEntry("雲林環保局", "雲林環保局", Privilege.defaultPrivilege))

  val getInfoList = map.values.toList

  def getGroupInfo(id: String) = {
    map.getOrElse(id, adminGroup._2)
  }
}