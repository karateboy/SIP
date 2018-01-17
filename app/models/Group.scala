package models

case class GroupInfo(id: String, name: String, privilege: Privilege)
object Group extends Enumeration {
  val Admin = Value

  def mapEntry(_id: Group.Value, name: String, privilege: Privilege) =
    _id -> GroupInfo(_id.toString, name, privilege)

  val map = Map(
    mapEntry(Admin, "系統管理員", Privilege.defaultPrivilege))

  val getInfoList = map.values.toList
  def getGroupInfo(id: String) = {
    val group = Group.withName(id)
    map(group)
  }
}