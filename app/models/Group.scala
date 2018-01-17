package models

case class GroupInfo(id: String, name: String, privilege: Privilege)
object Group extends Enumeration {
  val Admin = Value

  val map = Map(
    Admin -> ("系統管理員", Privilege.emptyPrivilege)
   )

  def getInfoList = map.map { m => GroupInfo(m._1.toString, m._2._1, m._2._2) }.toList
}