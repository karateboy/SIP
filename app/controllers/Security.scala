package controllers
import play.api._
import play.api.mvc.Security._
import play.api.mvc._
import scala.concurrent._

class AuthenticatedRequest[A](val userinfo:String, request: Request[A]) extends WrappedRequest[A](request)

object Security {
  val idKey = "ID"
  val nameKey = "Name"
  val groupKey = "Group"
  case class UserInfo(id:String, name:String, groupId:String)
  

  def getUserinfo(request: RequestHeader):Option[UserInfo] = {
    val optId = request.session.get(idKey)
    if(optId.isEmpty)
      return None
      
    val optGroup = request.session.get(groupKey)
    if(optGroup.isEmpty)
      return None
      
    val optName = request.session.get(nameKey)
    if(optName.isEmpty)
      return None
          
    Some(UserInfo(optId.get, optName.get, optGroup.get))
  }
  
  def onUnauthorized(request: RequestHeader) = {
    Results.Unauthorized("Login first...")
  }
  
  //def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
  //  AuthenticatedBuilder(getUserinfo _, onUnauthorized)
  //})
  
  //def isAuthenticated(f: => String => Request[AnyContent] => Result) = {
  //  Authenticated(getUserinfo, onUnauthorized) { user =>
  //    Action(request => f(user)(request))
  //  }
  // }
  
  def setUserinfo[A](request: Request[A], userInfo:UserInfo)={
    request.session + 
      (idKey->userInfo.id.toString()) + (groupKey->userInfo.groupId.toString()) + 
      (nameKey->userInfo.name) 
  }
  
  def getUserInfo[A]()(implicit request:Request[A]):Option[UserInfo]={
    getUserinfo(request)
  }
  
  def Authenticated = new AuthenticatedBuilder(getUserinfo, onUnauthorized)
}