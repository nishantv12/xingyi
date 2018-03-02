package org.validoc.utils.functions

import org.validoc.utils.concurrency.DelayedFuture
import org.validoc.utils.local.{ExecutionContextWithLocal, LocalOpsForScalaFuture}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class MultipleExceptions(val first: Throwable, val seq: List[Throwable]) extends Exception(s"Exceptions are\n ${(first :: seq).map(_.getClass.getName).mkString("\n")}", first)


class AsyncForScalaFuture(implicit ex: ExecutionContextWithLocal) extends Async[Future] with MonadCanFailWithException[Future, Throwable] {
  private def wrap[T](fn: Try[T] => Unit)(tryT: Try[T]): Try[T] = try {
    fn(tryT)
    tryT
  } catch {
    case e: Exception => tryT
  }
  override def foldWithExceptionAndFail[T, T1](m: Future[T], fnE: Throwable => Future[T1], fnFailure: Throwable => Future[T1], fn: T => Future[T1]) = m.transformWith(_ match {
    case Success(t) => fn(t)
    case Failure(t) => fnE(t)
  })
  override def recover[T](m: Future[T], fn: Exception => Future[T]) = m.recoverWith { case e: Exception => fn(e) }

  override def respond[T](m: Future[T], fn: Try[T] => Unit): Future[T] = m.transform(wrap(fn))
  override def liftM[T](t: T): Future[T] = Future.successful(t)
  override def await[T](m: Future[T]): T = Await.result(m, 5 seconds)

  override def exception[T](t: Throwable) = Future.failed(t)
  override def map[T, T1](m: Future[T], fn: T => T1): Future[T1] = m.map(fn)
  override def flatMap[T, T1](m: Future[T], fn: T => Future[T1]): Future[T1] = m.flatMap(fn)
  override def fail[T](f: Throwable): Future[T] = Future.failed(f)
  override def flattenM[T](seq: Seq[Future[T]]): Future[Seq[T]] = Future.sequence(seq)
  override def async[T](t: => T) = Future(t)
  override def delay[T](duration: Duration)(block: => Future[T]): Future[T] = DelayedFuture(duration)(block)
  override def mapEither[T, T1](m: Future[T], fn: Either[Throwable, T] => Future[T1]): Future[T1] = m.transformWith {
    case Success(t) => fn(Right(t))
    case Failure(t) => fn(Left(t))
  }

}

object AsyncForScalaFuture {

  object ImplicitsForTest {
    implicit val executionContext = new ExecutionContextWithLocal(ExecutionContext.Implicits.global)
  }

  implicit val LocalOpsForScalaFuture = new LocalOpsForScalaFuture
  implicit def defaultAsyncForScalaFuture(implicit ec: ExecutionContextWithLocal) = new AsyncForScalaFuture

}
