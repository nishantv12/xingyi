package org.validoc.sampleServer

import java.util.ResourceBundle
import java.util.concurrent.Executors

import org.validoc.caffeine.CaffeineCache
import org.validoc.sample.{JsonBundle, PromotionSetup}
import org.validoc.sample.domain.{MostPopular, MostPopularQuery, SampleJsonsForCompilation}
import org.validoc.simpleServer.{EndpointHandler, SimpleHttpServer}
import org.validoc.utils.functions.AsyncForScalaFuture.ImplicitsForTest._
import org.validoc.utils.functions.AsyncForScalaFuture._
import org.validoc.utils.http._
import org.validoc.utils.json.FromJson
import org.validoc.utils.logging.{AbstractLogRequestAndResult, LogRequestAndResult, LogRequestAndResultForBundle, PrintlnLoggingAdapter}
import org.validoc.utils.metrics.PrintlnPutMetrics
import org.validoc.utils.parser.Parser
import org.validoc.utils.tagless.{HttpFactory, TaglessLanguageLanguageForKleislis}

import scala.concurrent.Future

object SampleServer extends App with SampleJsonsForCompilation {

  implicit val httpFactory = new HttpFactory[Future, ServiceRequest, ServiceResponse] {
    override def apply(v1: ServiceName) = { req => Future.successful(ServiceResponse(Status(200), Body(s"response; ${req.body.map(_.s).getOrElse("")}"), ContentType("text/html"))) }
  }
  implicit val loggingAdapter = PrintlnLoggingAdapter
  implicit val resourceBundle = ResourceBundle.getBundle("messages")
  implicit val putMetrics = PrintlnPutMetrics
  implicit val logRequestAndResult: LogRequestAndResult[Throwable] = new AbstractLogRequestAndResult[Throwable] {
    override protected def format(messagePrefix: String, messagePostFix: String)(strings: String*) = messagePostFix + "." + messagePostFix + ":" + strings.mkString(",")
  }
  implicit val cacheFactory = CaffeineCache.cacheFactoryForFuture(CaffeineCache.defaultCacheBuilder)

  val interpreter = new TaglessLanguageLanguageForKleislis[Future, Throwable]

  implicit val jsonBundle: JsonBundle = JsonBundle()

  implicit val executors = Executors.newFixedThreadPool(10)


  val setup = new PromotionSetup[interpreter.EndpointK, interpreter.Kleisli, Future, Throwable](interpreter.NonFunctionalLanguageService())


  new SimpleHttpServer(9000, new EndpointHandler[Future, Throwable](setup.microservice)).start()
}
