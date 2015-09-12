package modules.silhouette.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
 * The form which handles the submission of the credentials.
 */
object SignInForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "persist" -> optional(boolean))(Data.apply)(Data.unapply))

  /**
   * The form data.
   *
   * @param email The email of the user.
   * @param password The password of the user.
   * @param rememberMe Indicates if the user should stay logged in on the next visit.
   */
  case class Data(
    email: String,
    password: String,
    persist: Option[Boolean])

  /**
   * The companion object.
   */
  object Data {

    /**
     * Converts the [Date] object to Json and vice versa.
     */
    implicit val jsonFormat = Json.format[Data]
  }

}
