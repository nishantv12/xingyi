package org.validoc.utils.service

import org.validoc.utils.http.{FromServiceRequest, ServiceRequest, ToServiceRequest, ToServiceResponse}
import org.validoc.utils.metrics.PutMetrics
import org.validoc.utils.success.Succeeded
import org.validoc.utils.time.NanoTimeService

class ServerContext[HttpReq, HttpRes](implicit val timeService: NanoTimeService,
                                      val putMetrics: PutMetrics,
                                      val succeeded: Succeeded[HttpRes],
                                      val toServiceRequest: ToServiceRequest[HttpReq],
                                      val toServiceResponse: ToServiceResponse[HttpRes],
                                      val fromServiceRequest: FromServiceRequest[HttpReq]) {

}
