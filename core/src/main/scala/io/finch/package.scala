package io

import com.twitter.finagle.httpx.Response
import com.twitter.util.Future
import io.finch.response.EncodeResponse

/**
 * This is a root package of the Finch library, which provides an immutable layer of functions and types atop of Finagle
 * for writing lightweight HTTP services. It roughly contains three packages: [[io.finch.route]], [[io.finch.request]],
 * [[io.finch.response]].
 */
package object finch
  extends Endpoints
  with Outputs {

  /**
   * Alters any object within a `toFuture` method.
   *
   * @param any an object to be altered
   *
   * @tparam A an object type
   */
  implicit class AnyOps[A](val any: A) extends AnyVal {

    /**
     * Converts this ''any'' object into a ''Future''
     */
    def toFuture: Future[A] = Future.value[A](any)
  }

  /**
   * Alters any throwable with a `toFutureException` method.
   *
   * @param t a throwable to be altered
   */
  implicit class ThrowableOps(val t: Throwable) extends AnyVal {

    /**
     * Converts this throwable object into a `Future` exception.
     */
    def toFutureException[A]: Future[A] = Future.exception[A](t)
  }

  // Implicitly converts an `Endpoint.Output` to `Response`.
  implicit def outputToResponse[A](o: Endpoint.Output[A])(implicit e: EncodeResponse[A]): Response = {
    val rep = Response()
    // properties from `EncodeResponse`
    rep.content = e(o.value)
    rep.contentType = e.contentType
    e.charset.foreach { cs => rep.charset = cs }

    // properties from Output
    rep.status = o.status
    o.headers.foreach { case (k, v) => rep.headerMap.add(k, v) }
    o.cookies.foreach {
      rep.addCookie
    }
    o.contentType.foreach { ct => rep.contentType = ct }
    o.charset.foreach { cs => rep.charset = cs }

    rep
  }
}
