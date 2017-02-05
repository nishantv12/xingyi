package org.validoc.utils.aggregate

import org.validoc.utils.Service
import org.validoc.utils.concurrency.Async
import Async._

trait HasChildren[Parent, Child] {
  def apply(p: Parent): Seq[Child]
}

trait Enricher[Enriched, Parent, Child] {
  def apply(p: Parent)(children: Seq[Child]): Enriched
}

trait Merger[Result, First, Second] extends (((First, Second)) => Result)

class EnrichParentChildService[M[_] : Async, ReqP, ResP, ReqC, ResC, ResE](parentService: Service[M, ReqP, ResP],
                                                                           childService: Service[M, ReqC, ResC])
                                                                          (implicit children: HasChildren[ResP, ReqC],
                                                                           enricher: Enricher[ResE, ResP, ResC]) extends Service[M, ReqP, ResE] {
  override def apply(reqP: ReqP): M[ResE] = parentService(reqP).flatMap(resP => children(resP).map(childService).join.map(enricher(resP)))

}

class MergeService[M[_] : Async, ReqM, ResM, Req1, Res1, Req2, Res2](firstService: Service[M, Req1, Res1], secondService: Service[M, Req2, Res2])
                                                                    (implicit merger: Merger[ResM, Res1, Res2],
                                                                     reqMtoReq1: ReqM => Req1,
                                                                     reqMtoReq2: ReqM => Req2)
  extends Service[M, ReqM, ResM] {
  override def apply(req: ReqM): M[ResM] = {
    firstService(req) join secondService(req) map merger
  }
}
