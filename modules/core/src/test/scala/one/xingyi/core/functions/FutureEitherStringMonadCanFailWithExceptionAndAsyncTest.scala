package one.xingyi.core.functions

import one.xingyi.core.monad.AsyncForScalaFuture.ImplicitsForTest._
import one.xingyi.core.monad.{Async, AsyncForScalaFutureEither, Monad}

object FutureEitherStringMonadCanFailWithExceptionAndAsyncTest {
  val parent = new AsyncForScalaFutureEither[String]()
  implicit val futureEitherStringMonad = new parent.AsyncForFutureEither
}

import one.xingyi.core.functions.FutureEitherStringMonadCanFailWithExceptionAndAsyncTest._
import org.scalatest.FlatSpecLike

class FutureEitherStringMonadCanFailWithExceptionAndAsyncTest extends AbstractMonadCanFailWithFailWithExceptionNotAsThrowableTests[FutureEitherString, String] with FlatSpecLike with AbstractAsyncTests[FutureEitherString] {
  override def async: Async[FutureEitherString] = futureEitherStringMonad
  override def monad: Monad[FutureEitherString] = futureEitherStringMonad
  override def makeFail(s: String): String = s
  override def failToString(f: String): String = f
}