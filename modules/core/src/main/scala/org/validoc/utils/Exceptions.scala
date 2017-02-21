package org.validoc.utils

import org.validoc.utils.http.{HostName, RequestDetails, ServiceResponse, Status}
import org.validoc.utils.parser.ParserResult

case class GatewayException(requestDetails: RequestDetails[_], serviceResponse: ServiceResponse) extends
  Exception(s" RequestDetails $requestDetails\nResponse $serviceResponse")


case class UnexpectedException(requestDetails: RequestDetails[_], t: Throwable) extends
  Exception(s" RequestDetails $requestDetails\nNested: $t", t)

class ParserException(val parserResult: ParserResult[_]) extends Exception(parserResult.toString)

case class UnexpectedParserException(serviceResponse: ServiceResponse, t: Throwable) extends Exception(serviceResponse.toString, t)