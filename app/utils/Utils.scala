package utils

import scala.reflect.runtime.universe.{ Type, typeTag, _ }
import scala.reflect.{ ClassTag, ManifestFactory, classTag }

/**
 * some utils
 */
object Utils {

  def classTag2Manifest[T: ClassTag]: Manifest[T] = {
    val clazz = classTag[T].runtimeClass
    ManifestFactory.classType(clazz).asInstanceOf[Manifest[T]]
  }

  def typeTag2Manifest[T: TypeTag]: Manifest[T] = {
    val t = typeTag[T]
    val mirror = t.mirror
    def toManifestRec(t: Type): Manifest[_] = {
      val clazz = ClassTag[T](mirror.runtimeClass(t)).runtimeClass
      if (t.typeArgs.length == 1) {
        val arg = toManifestRec(t.typeArgs.head)
        ManifestFactory.classType(clazz, arg)
      } else if (t.typeArgs.length > 1) {
        val args = t.typeArgs.map(x => toManifestRec(x))
        ManifestFactory.classType(clazz, args.head, args.tail: _*)
      } else {
        ManifestFactory.classType(clazz)
      }
    }
    toManifestRec(t.tpe).asInstanceOf[Manifest[T]]
  }

  implicit class SafeGetOrElse[T](val o: Option[T]) {
    def safeGetOrElse(or: T): T = o match {
      case Some(v) => v
      case None => or
    }
  }

}