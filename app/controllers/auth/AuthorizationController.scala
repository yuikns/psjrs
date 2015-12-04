package controllers.auth

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import modules.silhouette.forms.SignUpForm
import modules.silhouette.models.User
import modules.silhouette.models.services.UserService
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import utils.StringHelper.ToJson

import scala.concurrent.Future

/**
 * The sign up controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class AuthorizationController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, JWTAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher)
  extends Silhouette[User, JWTAuthenticator] {

  /**
   * Registers a new user.
   * JSON Based
   * @return The result to display.
   */
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(
            BadRequest(
              Map[String, Any]("status" -> false,
                "message" -> Messages("user.exists")).toJson)
              .as("application/json"))
        case None =>
          val authInfo = passwordHasher.hash(data.password)
          val user = User(
            userID = UUID.randomUUID(),
            loginInfo = loginInfo,
            firstName = Some(data.firstName),
            lastName = Some(data.lastName),
            fullName = Some(data.firstName + " " + data.lastName),
            email = Some(data.email),
            avatarURL = None)
          for {
            avatar <- avatarService.retrieveURL(data.email)
            user <- userService.save(user.copy(avatarURL = avatar))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.
              embed(
                value,
                Ok(Map[String, Any]("status" -> true,
                  "token" -> value).toJson)
                  .as("application/json"))
          } yield {
            env.eventBus.publish(SignUpEvent(user, request, request2Messages))
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(
          InternalServerError(
            Map[String, Any]("status" -> false,
              "message" -> Messages("data.invalid", error)).toJson)
            .as("application/json"))
    }
  }

  def signOut = SecuredAction.async { implicit request =>
    try {
      env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
      Future.successful(Ok(Map[String, Any]("status" -> true, "message" -> Messages("user.signout")).toJson))
    } catch {
      case t: Throwable =>
        Future.successful(
          InternalServerError(
            Map[String, Any]("status" -> false,
              "message" -> Messages("user.signout_error", t.getLocalizedMessage)).toJson).
            as("application/json"))
    }
  }

}
