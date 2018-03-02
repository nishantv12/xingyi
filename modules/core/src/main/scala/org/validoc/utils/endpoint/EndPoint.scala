package org.validoc.utils.endpoint

import org.validoc.utils._
import org.validoc.utils.functions.Monad
import org.validoc.utils.http._
import org.validoc.utils.strings.Strings

import scala.concurrent.Future
import scala.language.higherKinds


trait MatchesServiceRequest {
  def apply(endpointName: String)(serviceRequest: ServiceRequest): Boolean
}

case class EndPoint[M[_] : Monad, Req, Res](normalisedPath: String, matchesServiceRequest: MatchesServiceRequest)(kleisli: Req => M[Res])
                                           (implicit fromServiceRequest: FromServiceRequest[M, Req], toServiceResponse: ToServiceResponse[Res]) extends (ServiceRequest => Option[M[ServiceResponse]]) {
  override def apply(serviceRequest: ServiceRequest): Option[M[ServiceResponse]] =
    matchesServiceRequest(normalisedPath)(serviceRequest).toOption((fromServiceRequest |==> kleisli |=> toServiceResponse) (serviceRequest))
}

object MatchesServiceRequest {
  def fixedPath(method: Method) = FixedPathAndVerb(method)
  def idAtEnd(method: Method) = IdAtEndAndVerb(method)
}

case class FixedPathAndVerb(method: Method) extends MatchesServiceRequest {
  override def apply(endpointName: String)(serviceRequest: ServiceRequest): Boolean = serviceRequest.method == method && serviceRequest.uri.asUriString == endpointName
}

case class IdAtEndAndVerb(method: Method) extends MatchesServiceRequest {
  val startFn = Strings.allButlastSection("/") _
  override def apply(endpointName: String)(serviceRequest: ServiceRequest): Boolean = startFn(serviceRequest.uri.asUriString) == endpointName && serviceRequest.method == method
}

import org.validoc.utils.functions.AsyncForScalaFuture._
import org.validoc.utils.functions.AsyncForScalaFuture.ImplicitsForTest._




