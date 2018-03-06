package org.validoc.utils.endpoint

import org.validoc.utils.UtilsSpec
import org.validoc.utils.functions.{Functions, Monad, ScalaFutureAsAsyncAndMonad}
import org.validoc.utils.http._
import org.validoc.utils.language.Language._

import scala.concurrent.Future
import org.mockito.Mockito._
import org.validoc.utils.functions.AsyncForScalaFuture._
import ImplicitsForTest._

class EndpointSpec extends UtilsSpec with ServiceRequestForEndpointFixture {

  behavior of "Endpoint"


  def setup(fn: (EndPoint[Future, String, String], MatchesServiceRequest, String => Future[String]) => Unit) = {
    val matchesServiceRequest = MatchesServiceRequest.fixedPath(Get)
    val delegate = mock[String => Future[String]]
    implicit object fromServiceRequestForString extends FromServiceRequest[Future, String] {
      override def apply(v1: ServiceRequest) = Future.successful(v1.body.map(_.s).getOrElse("") + "_fromSR")
    }
    implicit object toServiceResponseForString extends ToServiceResponse[String] {
      override def apply(v1: String) = ServiceResponse(Status(123), Body(v1 + "_toSR"), ContentType("someContentType"))
    }
    val endPointKleisli = new EndpointKleisli[Future] with ScalaFutureAsAsyncAndMonad
    val endPoint = endPointKleisli.endpoint[String, String]("/some/path", matchesServiceRequest)(delegate)
    fn(endPoint, matchesServiceRequest, delegate)
  }

  it should "have a isDefinedAt that delegates to the matchesServiceRequest" in { // I would use mockito but we are currying and thus that's not going to work well
    setup { (endpoint, matcher, delegate) =>
      endpoint.isDefinedAt(srGetPath) shouldBe true
      endpoint.isDefinedAt(srGetPathWithId) shouldBe false
    }
  }

  it should "have a apply method that doesn't call the kleisli if the service request doesn't match" in {
    setup { (endpoint, matcher, delegate) =>
      endpoint(srGetPathWithId) shouldBe None
    }
  }
  it should "have a apply method that calls the kleisli if the service request  matchs" in {
    setup { (endpoint, matcher, delegate) =>
      when(delegate.apply("someGetPathBody_fromSR")) thenReturn Future.successful("someResult")
      endpoint(srGetPath).get.await shouldBe ServiceResponse(Status(123), Body("someResult_toSR"), ContentType("someContentType"))
    }
  }

  it should "have a toString" in {
    setup { (endpoint, matcher, delegate) =>
      endpoint.toString() shouldBe "Endpoint(/some/path, FixedPathAndVerb(Get))"
    }
  }

  it should "have a debugInfo for a success" in {
    setup { (endpoint, matcher, delegate) =>
      endpoint.debugInfo(srGetPath) shouldBe "Endpoint(/some/path, FixedPathAndVerb(Get)) called with ServiceRequest(Get,Uri(/some/path?a=1),None,None,List(),Some(Body(someGetPathBody))) results in true"
    }
  }
  it should "have a debugInfo for a fail" in {
    setup { (endpoint, matcher, delegate) =>
      endpoint.debugInfo(srPutPath) shouldBe "Endpoint(/some/path, FixedPathAndVerb(Get)) called with ServiceRequest(Put,Uri(/some/path?a=1),None,None,List(),Some(Body(somePutPathBody))) results in false"
    }
  }
}
