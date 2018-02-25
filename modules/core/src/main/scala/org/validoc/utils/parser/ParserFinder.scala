package org.validoc.utils.parser

import org.validoc.utils.http.ContentType
import org.validoc.utils.json.FromJson
import org.validoc.utils.{ParserException, ParserNotFoundException}

import scala.annotation.implicitNotFound

trait Parser[T] extends (String => T)

sealed trait ParserResult[Result] {
  def map[Result2](fn: Result => Result2): ParserResult[Result2]

  def valueOrDefault(default: Result): Result

  def valueOrException: Result
}

case class FoundResult[Result](contentType: ContentType, result: Result) extends ParserResult[Result] {
  override def map[Result2](fn: (Result) => Result2): ParserResult[Result2] = FoundResult(contentType, fn(result))

  override def valueOrDefault(default: Result): Result = result

  override def valueOrException: Result = result
}


trait FailedParserResult[Result] extends ParserResult[Result] {
  def valueOrDefault(default: Result) = default

  def valueOrException = throw new ParserException(this)
}

case class NotFoundResult[Result](contentType: ContentType, legalContentTypes: Set[ContentType]) extends FailedParserResult[Result] {
  override def map[Result2](fn: (Result) => Result2): ParserResult[Result2] = NotFoundResult(contentType, legalContentTypes)
}

case class ErrorResult[Result](contentType: ContentType, e: Throwable) extends FailedParserResult[Result] {
  override def map[Result2](fn: (Result) => Result2): ParserResult[Result2] = ErrorResult(contentType, e)
}


@implicitNotFound("ParserFinder of type [${T}] not found. Is it because you need another implicit in scope? The simplest way to get one in scope if there is only one JSON representation is to implement FromJson[T]" )
trait ParserFinder[T] extends ((ContentType, String) => ParserResult[T]) {

  def find(contentType: ContentType): ParserResult[Parser[T]]

  override def apply(v1: ContentType, v2: String): ParserResult[T] = find(v1).map(_ (v2))

  def findAndParse(v1: ContentType, v2: String) = {
    apply(v1, v2) match {
      case FoundResult(_, result) => result
      case nf: FailedParserResult[T] => throw new ParserNotFoundException(nf)
    }
  }
}

case class AlwaysParserFinder[T](parser: Parser[T]) extends ParserFinder[T] {
  override def find(contentType: ContentType): ParserResult[Parser[T]] = FoundResult(contentType, parser)
}

case class MapParserFinder[T](map: Map[ContentType, Parser[T]]) extends ParserFinder[T] {
  override def find(contentType: ContentType): ParserResult[Parser[T]] = map.get(contentType) match {
    case Some(parser) => FoundResult(contentType, parser)
    case None => NotFoundResult(contentType, map.keySet)
  }
}

object ParserFinder {
  implicit def alwaysSameParser[T](implicit fromJson: FromJson[T]): ParserFinder[T] = always(fromJson)

  def always[T](parser: Parser[T]) = AlwaysParserFinder(parser)

  def fromMap[T](map: Map[ContentType, Parser[T]]) = MapParserFinder(map)
}