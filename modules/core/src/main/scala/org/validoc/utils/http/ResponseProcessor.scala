package org.validoc.utils.http

import org.validoc.utils.functions.{Liftable, MonadCanFail}
import org.validoc.utils.parser.Parser

import scala.annotation.implicitNotFound
import scala.language.higherKinds
import org.validoc.utils._

@implicitNotFound(
  """Cannot find ResponseProcessor[${M},${Req},${Res}] The easiest way is to have a ResponseParser and a Failer in scope
     To check this you can try
        val failer = implicitly[Failer[<whatever your fail type is>]]
        val responseParser = implicitly[ResponseParser[${M},${Req},${Res}]]
        the compiler should give you a more helpful message then
  """)
trait ResponseProcessor[M[_], Req, Res] extends (ResponseState[Req] => M[Res])

trait ResponseParser[Fail, Req, Res] extends (Req => String => Either[Fail, Res])

object ResponseParser {
  def defaultDirtyParser[M[_], Fail, Req, Res](implicit parser: Parser[Res]) = new ResponseParser[Fail, Req, Res] {
    override def apply(req: Req) = { string => Right(parser(string))
    }
  }
}

trait Failer[Fail] {
  def notFound[Req](req: Req, response: ServiceResponse): Fail
  def unexpected[Req](req: Req, response: ServiceResponse): Fail
  def exception[Req](req: Req, throwable: Throwable): Fail
  def idNotFind(serviceRequest: ServiceRequest): Fail
  def pathNotFound(serviceRequest: ServiceRequest): Fail
}

object Failer {


  implicit object FailerForThrowable extends Failer[Throwable] {
    override def notFound[Req](req: Req, response: ServiceResponse) = ???
    override def unexpected[Req](req: Req, response: ServiceResponse) = ???
    override def exception[Req](req: Req, throwable: Throwable) = ???
    override def idNotFind(serviceRequest: ServiceRequest): Throwable = ???
    override def pathNotFound(serviceRequest: ServiceRequest): Throwable = ???
  }

}

object ResponseProcessor {

  implicit def defaultResponseProcessor[M[_], Fail, Req, Res](implicit monadCanFail: MonadCanFail[M, Fail], parser: ResponseParser[Fail, Req, Res], failer: Failer[Fail]) = new ResponseProcessor[M, Req, Res] {
    override def apply(state: ResponseState[Req]) = {
      state match {
        case ResponseOk(req, response) => parser(req)(response.body.s).liftEither[M]
        case ResponseNotFound(req, response) => failer.notFound(req, response).fail
        case ResponseUnexpectedStatusCode(req, response) => failer.unexpected(req, response).fail
        case ResponseException(req, throwable) => failer.exception(req, throwable).fail
      }
    }
  }
}
