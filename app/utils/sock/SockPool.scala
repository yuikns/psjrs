package utils.sock

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.commons.pool.impl.GenericObjectPool.WHEN_EXHAUSTED_BLOCK

/**
 * Create a new <tt>SockPool</tt> using the specified values.
 * @param maxActive the maximum number of objects that can be borrowed from me at one time (see [[org.apache.commons.pool.impl.GenericObjectPool#setMaxActive]])
 * @param whenExhaustedAction the action to take when the pool is exhausted (see [[org.apache.commons.pool.impl.GenericObjectPool#getWhenExhaustedAction]])
 * @param maxWait the maximum amount of time to wait for an idle object when the pool is exhausted an and
 *                <i>whenExhaustedAction</i> is [[org.apache.commons.pool.impl.GenericObjectPool#WHEN_EXHAUSTED_BLOCK]] (otherwise ignored)
 *                (see [[org.apache.commons.pool.impl.GenericObjectPool#getMaxWait]])
 * @param host host of socket server
 * @param port port of socket server
 * @param timeout timeout in ms of socket connect, 0 for default and will never timeout
 */
case class SockPool(host: String, port: Int, timeout: Int = 0, maxActive: Int = 32, whenExhaustedAction: Byte = WHEN_EXHAUSTED_BLOCK, maxWait: Long = 0) {

  val pool = {
    val p = new GenericObjectPool(new SockObjectFactory(host, port, timeout), maxActive, whenExhaustedAction, maxWait)
    println("[connected to socket] " + p.getMaxActive + " # " + p.getMaxIdle + "#" + p.getWhenExhaustedAction + "#vs.#" + WHEN_EXHAUSTED_BLOCK + " # " + p.getMaxWait + " info: " + toString())
    p
  }

  var counter = 0

  override def toString = "[SockBufSockBuffer] " + host + ":" + String.valueOf(port)

  def withClient[T](body: SockBuffer => T): T = {
    val client = pool.borrowObject
    try {
      body(client)
    } catch {
      case t: Throwable =>
        client.activate()
        throw t
    } finally {
      pool.returnObject(client)
    }
  }

  // close pool & free resources
  def close() = pool.close()

  /**
   * @param host host name
   * @param port port
   * @param timeout timeout in ms
   */
  // pool size
  private class SockObjectFactory(val host: String, val port: Int, val timeout: Int = 0)
    extends PoolableObjectFactory[SockBuffer] {

    // when we make an object it's already connected
    override def makeObject: SockBuffer = {
      pool.synchronized{
        counter += 1
        println(s"new object , count : $counter")
      }
      SockBuffer(host, port, timeout)
    }

    // quit & disconnect
    override def destroyObject(rc: SockBuffer): Unit = {
      pool.synchronized{
        counter -= 1
        println(s"remove object , count : $counter")
      }
      rc.close()
    }

    /**
     * Uninitialize an instance to be returned to the idle object pool.
     *
     * @param rc the instance to be passivated
     * @see #destroyObject
     */
    override def passivateObject(rc: SockBuffer): Unit = {
      //println("Do : passivateObject")
    }

    override def validateObject(rc: SockBuffer): Boolean = {
      println(s"valid status: " + (!rc.isClosed && rc.isConnected))
      !rc.isClosed && rc.isConnected
    }

    /**
     * Reinitialize an instance to be returned by the pool.
     * @param rc socket sc instance
     */
    override def activateObject(rc: SockBuffer): Unit = {
      //println("Do : activateObject")
      if (!rc.isConnected || rc.isClosed) {
        println("reconnect ..")
        rc.activate()
      }
    }

  }

}
