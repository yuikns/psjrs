package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import modules.silhouette.models.User
import play.api.i18n.{ Messages, MessagesApi }
import play.api.mvc.Action
import utils.StringHelper.ToJson

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, JWTAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, JWTAuthenticator] {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = Action.async(
    Future.successful(
      Ok(
        Map[String, Any](
          "status" -> true,
          "message" -> Messages("banner.sys")).toJson(false))))

}
