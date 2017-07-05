package org.validoc.utils.service


import org.validoc.utils.concurrency.Async
import org.validoc.utils.functions.SemiGroup
import org.validoc.utils.http.{HostName, MakeHttpService, Port}

import scala.language.higherKinds
import scala.reflect.ClassTag

trait ServiceReporter[Service] extends (Service => Option[String])

object ServiceReporter {
  implicit def defaultServiceReporter[Service] = new ServiceReporter[Service] {
    override def apply(v1: Service): Option[String] = None
  }
}

abstract class ServiceDescription[M[_], Req, Res](implicit serviceReporter: ServiceReporter[Req => M[Res]]) {
  type FoldFn[T] = (ServiceDescription[M, _, _], Int) => T

  def service: (Req => M[Res])

  def report: Option[String] = serviceReporter(service)

  def fold[T](fn: FoldFn[T], depth: Int)(implicit group: SemiGroup[T]): T

  def description: String
}


case class RootHttpServiceDescription[M[_], HttpReq, HttpRes](hostName: HostName, port: Port, makeHttpService: MakeHttpService[M, HttpReq, HttpRes])
                                                             (implicit serviceReporter: ServiceReporter[HttpReq => M[HttpRes]])
  extends ServiceDescription[M, HttpReq, HttpRes] {

  lazy val service = makeHttpService.create(hostName, port)


  override def description = s"RootHttp(${hostName},$port)"

  override def fold[T](fn: FoldFn[T], depth: Int)(implicit group: SemiGroup[T]): T = fn(this, depth)
}

case class DelegateServiceDescription[M[_], OldReq, OldRes, Req, Res, Service <: Req => M[Res] : ClassTag]
(delegate: ServiceDescription[M, OldReq, OldRes], serviceMaker: (OldReq => M[OldRes]) => Service)
(implicit serviceReporter: ServiceReporter[Service]) extends ServiceDescription[M, Req, Res] {

  val serviceClass = implicitly[ClassTag[Service]].runtimeClass

  lazy val service = serviceMaker(delegate.service)

  override def description = serviceClass.getSimpleName

  override def fold[T](fn: FoldFn[T], depth: Int)(implicit group: SemiGroup[T]): T = group.add(fn(this, depth), delegate.fold(fn, depth + 1))
}

case class ParamDelegateServiceDescription[M[_], Param, OldReq, OldRes, Req, Res, Service <: Req => M[Res] : ClassTag]
(param: Param, delegate: ServiceDescription[M, OldReq, OldRes], serviceMaker: (Param, OldReq => M[OldRes]) => Service)
(implicit serviceReporter: ServiceReporter[Service]) extends ServiceDescription[M, Req, Res] {

  val serviceClass = implicitly[ClassTag[Service]].runtimeClass

  lazy val service = serviceMaker(param, delegate.service)


  override def description = s"${serviceClass.getSimpleName}($param)"

  override def fold[T](fn: FoldFn[T], depth: Int)(implicit group: SemiGroup[T]): T = group.add(fn(this, depth), delegate.fold(fn, depth + 1))
}


case class MergingTwoServicesDescription[M[_] : Async, Req1, Res1, Req2, Res2, Req: ClassTag, Res: ClassTag, Service <: Req => M[Res]]
(service1: ServiceDescription[M, Req1, Res1],
 service2: ServiceDescription[M, Req2, Res2],
 maker: (Req1 => M[Res1], Req2 => M[Res2]) => Service)

  extends ServiceDescription[M, Req, Res] {

  override lazy val service: (Req) => M[Res] = maker(service1.service, service2.service)


  override def description: String = s"Merging[${implicitly[ClassTag[Req]].runtimeClass.getSimpleName},${implicitly[ClassTag[Res]].runtimeClass.getSimpleName}]"

  override def fold[T](fn: FoldFn[T], depth: Int)(implicit group: SemiGroup[T]): T = group.add(fn(this, depth), group.add(service1.fold(fn, depth + 1), service2.fold(fn, depth + 1)))
}


trait MakeServiceDescription[M[_], OldReq, OldRes, Req, Res] {
  def apply(delegate: ServiceDescription[M, OldReq, OldRes]): ServiceDescription[M, Req, Res]
}
