package utils.play

import javax.inject.Inject

import com.mohiva.play.silhouette.api.SecuredErrorHandler
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }
import play.api.routing.Router
import play.api.{ Configuration, OptionalSourceMapper }
import utils.StringHelper.ToJson

import scala.concurrent.Future

/**
 * A secured error handler.
 */
class ErrorHandler @Inject() (
  env: play.api.Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: javax.inject.Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
  with SecuredErrorHandler {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param messages The messages for the current language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, messages: Messages): Option[Future[Result]] = {
    Option(Future.successful(Unauthorized(Map[String, Any]("status" -> false, "message" -> "http_layer.not_authenticated").toJson)))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param messages The messages for the current language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, messages: Messages): Option[Future[Result]] = {
    Option(Future.successful(Unauthorized(Map[String, Any]("status" -> false, "message" -> "http_layer.not_authorized").toJson)))
  }

  /**
   * Here, I will override a set of error in DefaultHttpErrorHandler
   * // https://www.playframework.com/documentation/2.4.x/api/java/play/http/DefaultHttpErrorHandler.html
   */

  /**
   * Invoked when a client error occurs, that is, an error in the 4xx series.
   */
  //  override def onClientError(request: RequestHeader, ecode: Int, error: String): Future[Result] = Future.successful(Forbidden({
  //    Map[String, Any]("status" -> false, "message" -> "client_error", "error" -> ecode).toJson(false)
  //  }))

  override def onBadRequest(request: RequestHeader, error: String) = Future.successful(BadRequest({
    Map[String, Any]("status" -> false, "message" -> "http_layer.bad_request", "uri" -> request.uri).toJson(false)
  }))

  override def onForbidden(request: RequestHeader, error: String) = Future.successful(Forbidden({
    println("request_forbidden:" + error)
    Map[String, Any]("status" -> false, "message" -> "http_layer.forbidden", "uri" -> request.uri).toJson(false)
  }))

  override def onNotFound(request: RequestHeader, error: String) = Future.successful(NotFound({
    Map[String, Any]("status" -> false, "message" -> "http_layer.action_not_found", "uri" -> request.uri).toJson(false)
  }))

  override def onOtherClientError(request: RequestHeader, ecode: Int, error: String) = Future.successful(BadRequest({
    Map[String, Any]("status" -> false, "message" -> "http_layer.other_client_error", "error" -> ecode).toJson(false)
  }))

  override def onServerError(request: RequestHeader, t: Throwable) = Future.successful(InternalServerError({
    t.printStackTrace()
    Map[String, Any]("status" -> false, "message" -> "http_layer.internal_server_error").toJson(false)
  }))

  override def logServerError(request: RequestHeader, e: play.api.UsefulException) = {
    println("utils.play.ErrorHandler::logServerError => " + e.getLocalizedMessage + "#" + e.getStackTrace.mkString(" << "))
  }

  override def onDevServerError(request: RequestHeader, e: play.api.UsefulException) = Future.successful(InternalServerError({
    Map[String, Any]("status" -> false, "message" -> "http_layer.dev_server_error").toJson(false)
  }))

  override def onProdServerError(request: RequestHeader, e: play.api.UsefulException) = Future.successful(InternalServerError({
    Map[String, Any]("status" -> false, "message" -> "http_layer.prod_server_error").toJson(false)
  }))

}
