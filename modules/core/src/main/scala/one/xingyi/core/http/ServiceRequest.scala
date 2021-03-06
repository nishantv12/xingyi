/** Copyright (c) 2018, Phil Rice. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package one.xingyi.core.http

import one.xingyi.core.language.Language._
import one.xingyi.core.monad.Liftable

import scala.annotation.implicitNotFound
import scala.language.higherKinds

case class ServiceRequest(method: Method, uri: Uri, acceptHeader: Option[AcceptHeader] = None, contentType: Option[ContentType] = None, otherHeaders: List[Header] = List(), body: Option[Body] = None)

trait OriginalReq[Req] {
  def acceptHeader(req: Req): AcceptHeader
  def header(req: Req, name: String): Header
  def contentType(req: Req): ContentType
  def method(req: Req): Method
  def path(req: Req): Path
}

@implicitNotFound("""Missing ToServiceRequest[${T}] This is how we turn a query/request object (${T}) into a HTTP request. If ${T} is a http request have """)
trait ToServiceRequest[T] extends (T => ServiceRequest)

object ToServiceRequest {
  implicit object ToServiceRequestForServiceRequest extends ToServiceRequest[ServiceRequest] {
    override def apply(v1: ServiceRequest): ServiceRequest = v1
  }
}

@implicitNotFound("Missing FromServiceRequest[${T}]This is how we create a query/request (${T}) from an external clients HTTP request. It is isolated from exactly which webframework we are using.")
trait FromServiceRequest[M[_], T] extends (ServiceRequest => M[T])

object FromServiceRequest {
  implicit def FromServiceResponseForServiceResponse[M[_] : Liftable] = new FromServiceRequest[M, ServiceRequest] {
    override def apply(v1: ServiceRequest): M[ServiceRequest] = v1.liftM
  }
}
