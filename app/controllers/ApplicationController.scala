package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import modules.silhouette.models.User
import play.api.i18n.{ Messages, MessagesApi }
import play.api.mvc.Action
import utils.StringHelper._
import java.net.URL
import java.net.URLEncoder
import scala.io.Source
import scala.concurrent.Future
import net.liftweb.json._

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


  /**
   * Handles the query action.(for test)
   *
   * @return The result to display.
   */
  def query(queryStr: String) = Action {
    val query = String.format("query=%s", URLEncoder.encode(queryStr, "UTF-8"));
    val queryRes = fromUrlWithTimeout(new URL("http://api.aminer.org/api/search/person?" + query),1000);

    val jsonRes = StringConverter(queryRes).parseJsonOrigin
    val JArray(results) = jsonRes \ "result"
    var respStr = new String("[")

    results.take(3).foreach{ result =>

      respStr += "\""
      implicit val formats = net.liftweb.json.DefaultFormats
      respStr += (result \ "name").extract[String]
      respStr += "\","
    }
    respStr = respStr.substring(0,respStr.lastIndexOf(","));
    respStr += "]"

    //response().setContentType("application/json");
    Ok.withHeaders(CONTENT_TYPE -> "application/json")
    Ok(respStr)
  }

  def fromUrlWithTimeout(url: URL, timeout: Int = 1500): String = {
    val conn = (url).openConnection()
    conn.setConnectTimeout(timeout)
    conn.setReadTimeout(timeout)
    val stream = conn.getInputStream()
    val src = (scala.util.control.Exception.catching(classOf[Throwable]) opt Source.fromInputStream(stream).mkString) match {
      case Some(s: String) => s
      case _ => ""
    }
    stream.close()
    src
  }

}

