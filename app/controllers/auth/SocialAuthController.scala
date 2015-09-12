package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import modules.silhouette.models.User
import modules.silhouette.models.services.UserService
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import utils.StringHelper.ToJson

import scala.concurrent.Future

/**
 * The social auth controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info service implementation.
 * @param socialProviderRegistry The social provider registry.
 */
class SocialAuthController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, JWTAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, JWTAuthenticator] with Logger {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(profile.loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, Ok(s"SocialAuthController::authenticate::$provider")) //Redirect(routes.ApplicationController.index())
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
        }
      case _ =>
        //Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
        Future.successful(
          BadRequest(
            Map[String, Any](
              "status" -> false,
              "message" -> Messages("request.provider_not_found",
                provider)).toJson))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        BadRequest(Map[String, Any]("status" -> false, "message" -> Messages("auth.failed")).toJson)
    }
  }
}
