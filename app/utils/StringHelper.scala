package utils

import net.liftweb.util.SecurityHelpers
import org.apache.commons.codec.digest.DigestUtils
import org.joda.time.DateTime

/**
 * Wrapper of String process
 */
object StringHelper {
  val alphabet: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
  val alphabetUC: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val jsonFormatsWithDateTime = {
    import net.liftweb.json._
    DefaultFormats + new DateTimeSerializer
  }

  def md5(s: String): String = DigestUtils.md5Hex(s)

  def sha1(s: String): String = SecurityHelpers.hashHex(s)

  def sha256(s: String): String = DigestUtils.sha256Hex(s)

  def sha512(s: String): String = DigestUtils.sha512Hex(s)

  def urlEncode(s: String): String = java.net.URLEncoder.encode(s, "utf-8")

  def urlDecode(s: String): String = java.net.URLDecoder.decode(s, "utf-8")

  def randomStr(len: Int): String = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to len) {
      sb.append(alphabetUC.charAt(r.nextInt.abs % alphabetUC.length))
    }
    sb.toString
  }

  def name2Token(name: String) = {
    name.toLowerCase.trim.replace("-", "").replace(". ", " ").replace(".", "")
  }

  def toJson(a: Any, compact: Boolean = true) = {
    implicit val formats = jsonFormatsWithDateTime
    if (compact) net.liftweb.json.Printer.compact(net.liftweb.json.JsonAST.render(
      net.liftweb.json.Extraction.decompose(a)))
    else net.liftweb.json.Printer.pretty(net.liftweb.json.JsonAST.render(
      net.liftweb.json.Extraction.decompose(a)))
  }

  def fromJson(s: String) = {
    //implicit val formats = net.liftweb.json.DefaultFormats
    implicit val formats = jsonFormatsWithDateTime
    net.liftweb.json.JsonParser.parse(s).values
  }

  implicit class StringHasher(val s: String) {
    def md5(): String = StringHelper.md5(s)

    def sha1(): String = StringHelper.sha1(s)

    def sha256(): String = StringHelper.sha256(s)

    def sha512(): String = StringHelper.sha512(s)
  }

  implicit class StringTester(val s: String) {
    lazy val isEmpty: Boolean = s == null || s.trim.length == 0
    lazy val isNotEmpty: Boolean = s != null && s.trim.length > 0
  }

  implicit class StringConverter(val s: String) {
    def safeToIntOrElse(or: Int) = safeToInt match {
      case Some(v: Int) => v
      case None => or
    }

    def safeToInt = scala.util.control.Exception.catching(classOf[java.lang.NumberFormatException]) opt s.toInt

    def safeToLongOrElse(or: Long) = safeToLong match {
      case Some(v: Long) => v
      case None => or
    }

    def safeToLong = scala.util.control.Exception.catching(classOf[java.lang.NumberFormatException]) opt s.toLong

    def safeToDoubleOrElse(or: Double) = safeToDouble match {
      case Some(v: Double) => v
      case None => or
    }

    def safeToDouble = scala.util.control.Exception.catching(classOf[java.lang.NumberFormatException]) opt s.toDouble

    def safeToBooleanOrElse(or: Boolean) = safeToBoolean match {
      case Some(v: Boolean) => v
      case None => or
    }

    def safeToBoolean = scala.util.control.Exception.catching(classOf[java.lang.IllegalArgumentException]) opt s.toBoolean

    def parseJsonToMapOrElse(or: Map[String, Any]) = parseJsonToMap match {
      case Some(v: Map[String, Any]) => v
      case None => or
    }

    def parseJsonToMap = scala.util.control.Exception.catching(classOf[java.lang.ClassCastException]) opt parseJson.asInstanceOf[Map[String, Any]]

    def parseJson = {
      //implicit val formats = net.liftweb.json.DefaultFormats
      implicit val formats = jsonFormatsWithDateTime
      net.liftweb.json.JsonParser.parse(s).values
    }

    def parseJsonToListOrElse(or: List[Any]) = parseJsonToList match {
      case Some(v: List[Any]) => v
      case None => or
    }

    def parseJsonToList = scala.util.control.Exception.catching(classOf[java.lang.ClassCastException]) opt parseJson.asInstanceOf[List[Any]]

    def parseJsonToClass[T: scala.reflect.ClassTag]: Option[T] = {
      //implicit val formats = net.liftweb.json.DefaultFormats
      implicit val formats = jsonFormatsWithDateTime
      implicit val mf = Utils.classTag2Manifest[T]
      scala.util.control.Exception.catching(classOf[java.lang.ClassCastException]) opt {
        net.liftweb.json.JsonParser.parse(s).extract[T]
      }
    }

  }

  class DateTimeSerializer extends net.liftweb.json.Serializer[DateTime] {

    import net.liftweb.json._

    private val DateTimeClass = classOf[DateTime]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
      case (TypeInfo(DateTimeClass, _), json) => json match {
        case JInt(millis) => new DateTime(millis.longValue)
        case x => throw new MappingException("Can't convert " + x + " to DateTime")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case t: DateTime => JInt(t.getMillis)
    }
  }

  implicit class ToJson(a: Any) {
    def toJson: String = toJson()

    def toJson(compact: Boolean = true): String = {
      StringHelper.toJson(a, compact)
      //      if (compact) net.liftweb.json.Printer.compact(net.liftweb.json.JsonAST.render(
      //        net.liftweb.json.Extraction.decompose(a)))
      //      else net.liftweb.json.Printer.pretty(net.liftweb.json.JsonAST.render(
      //        net.liftweb.json.Extraction.decompose(a)))
    }
  }

  // data minIng => Data Mining
  implicit class ToTitle(s: String) {
    def toTitle: String = {
      s.split("\\s+").flatMap { ones =>
        val tones = ones.trim
        if (tones.length > 0) {
          Option(ones.capitalize)
        } else {
          None
        }
      }.mkString(" ")
    }
  }

  implicit class ToAcronym(s: String) {
    def toNameAcronym: String = s.replace("-", "").toAcronym

    def toAcronym: String = {
      (s.replaceAll("[\\pP‘’“”]", " ").toLowerCase.split(" ") collect {
        case ones: String =>
          if (ones.length > 0) ones.charAt(0)
          else ""
      }).mkString("")
    }
  }

  implicit class ByteConvert(c: Byte) {
    def toLowerCase: Byte = if (c >= 'A' && c <= 'Z') (c + 32).toByte else c
  }

}
