package modules.silhouette.cache

import com.mohiva.play.silhouette.api.util.CacheLayer
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.StringHelper.{ StringConverter, ToJson }

import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

object SSDBCacheLayer {

  val data = MMap[String, String]() // use ssdb instead of this k-v will make the session more long

  def save[T](key: String, value: T, expiration: Long = 0): Future[T] = {
    //CacheSSDBClient.authTokenCacheSave(key, value.toJson, expiration)
    data.put(key, value.toJson)
    Future.successful(value)
  }

  def find[T: ClassTag](key: String): Future[Option[T]] = {
    //Future(CacheSSDBClient.authTokenCacheFind(key) match {
    Future(data.get(key) match {
      case Some(v) =>
        v.parseJsonToClass[T]
      case None =>
        None
    })
  }

  def remove(key: String) = {
    Future(doRemove(key))
  }

  private def doRemove(key: String) {
    //CacheSSDBClient.authTokenCacheDel(key)
    data.remove(key)
  }
}

class SSDBCacheLayer extends CacheLayer {

  /**
   * Save a value in cache.
   *
   * @param key The item key under which the value should be saved.
   * @param value The value to save.
   * @param expiration Expiration time in seconds (0 second means eternity).
   * @return The value saved in cache.
   */
  def save[T](key: String, value: T, expiration: Duration = Duration.Inf): Future[T] =
    SSDBCacheLayer.save(key, value, if (expiration == Duration.Inf) 0 else expiration.toSeconds)

  /**
   * Finds a value in the cache.
   *
   * @param key The key of the item to found.
   * @tparam T The type of the object to return.
   * @return The found value or None if no value could be found.
   */
  def find[T: ClassTag](key: String): Future[Option[T]] =
    SSDBCacheLayer.find(key)

  /**
   * Remove a value from the cache.
   *
   * @param key Item key.
   * @return An empty future to wait for removal.
   */
  def remove(key: String) =
    SSDBCacheLayer.remove(key)

}