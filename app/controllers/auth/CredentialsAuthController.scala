package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import modules.silhouette.forms.SignInForm
import modules.silhouette.models.User
import modules.silhouette.models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import utils.StringHelper.ToJson

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * The credentials auth controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param credentialsProvider The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 */
class CredentialsAuthController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, JWTAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock)
  extends Silhouette[User, JWTAuthenticator] {

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def authenticate = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      val credentials = Credentials(data.email, data.password)
      credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val c = configuration.underlying
            env.authenticatorService.create(loginInfo).map {
              case authenticator if data.persist.isDefined && data.persist.get =>
                authenticator.copy(
                  expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"))
              case authenticator => authenticator
            }.flatMap { authenticator =>
              env.eventBus.publish(LoginEvent(user, request, request2Messages))
              env.authenticatorService.init(authenticator).flatMap { v =>
                env.authenticatorService.embed(v,
                  Ok(Map[String, Any]("status" -> true,
                    "token" -> v).
                    toJson)
                    .as("application/json"))
              }
            }
          case None =>
            Future.failed(new IdentityNotFoundException(Messages("user.not_found")))
        }
      }.recover {
        case e =>
          Unauthorized(Map[String, Any]("status" -> false,
            "message" -> Messages("credentials.invalid")).toJson).
            as("application/json")
      }
    }.recoverTotal {
      case error =>
        println("error:" + error)
        Future.successful(
          InternalServerError(
            Map[String, Any]("status" -> false,
              "message" -> Messages("data.invalid")).toJson)
            .as("application/json"))
    }
  }
}
