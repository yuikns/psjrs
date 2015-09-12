package utils.play

import javax.inject.Inject

import controllers.Default
import play.api.Logger
import play.api.http.HttpFilters
import play.api.mvc.{ EssentialFilter, Filter, RequestHeader, Result }
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

case class CORSFilter() extends Filter {

  lazy val allowedDomain = play.api.Play.current.configuration.getString("cors.allowed.domain")

  def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    Logger.trace("[cors] filtering request to add cors")
    if (isPreFlight(request)) {
      Logger.trace("[cors] request is preflight")
      Logger.trace(s"[cors] default allowed domain is $allowedDomain")
      Future.successful(Default.Ok.withHeaders(
        "Access-Control-Allow-Origin" -> allowedDomain.orElse(request.headers.get("Origin")).getOrElse(""),
        "Access-Control-Allow-Methods" -> request.headers.get("Access-Control-Request-Method").getOrElse("*"),
        "Access-Control-Allow-Headers" -> request.headers.get("Access-Control-Request-Headers").getOrElse(""),
        "Access-Control-Expose-Headers" -> "WWW-Authenticate, Server-Authorization, X-Auth-Token",
        "Access-Control-Allow-Credentials" -> "true"))
    } else {
      Logger.trace("[cors] request is normal")
      Logger.trace(s"[cors] default allowed domain is $allowedDomain")
      f(request).map {
        _.withHeaders(
          "Access-Control-Allow-Origin" -> allowedDomain.orElse(request.headers.get("Origin")).getOrElse(""),
          "Access-Control-Allow-Methods" -> request.headers.get("Access-Control-Request-Method").getOrElse("*"),
          "Access-Control-Allow-Headers" -> request.headers.get("Access-Control-Request-Headers").getOrElse(""),
          "Access-Control-Expose-Headers" -> "WWW-Authenticate, Server-Authorization, X-Auth-Token, Authorization, Proxy-Authorization, Content-Type",
          "Access-Control-Allow-Credentials" -> "true")
      }
    }
  }

  def isPreFlight(r: RequestHeader) = (
    r.method.toLowerCase.equals("options")
    &&
    r.headers.get("Access-Control-Request-Method").nonEmpty)
}

/**
 * Provides filters.
 */
class Filters @Inject() (csrfFilter: CSRFFilter, securityHeadersFilter: SecurityHeadersFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(csrfFilter, securityHeadersFilter, CORSFilter())
}
