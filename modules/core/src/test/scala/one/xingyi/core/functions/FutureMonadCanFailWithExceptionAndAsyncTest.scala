package one.xingyi.core.functions

import one.xingyi.core.monad.AsyncForScalaFuture
import org.scalatest.FlatSpecLike
import one.xingyi.core.monad.AsyncForScalaFuture.ImplicitsForTest._
import one.xingyi.core.monad.AsyncForScalaFuture._

import scala.concurrent.Future

class FutureMonadCanFailWithExceptionAndAsyncTest extends AbstractMonadCanFailWithFailWithExceptionAsThrowableTests[Future] with FlatSpecLike with AbstractAsyncTests[Future] {
  override def async = AsyncForScalaFuture.defaultAsyncForScalaFuture
  override def monad = AsyncForScalaFuture.defaultAsyncForScalaFuture
}
