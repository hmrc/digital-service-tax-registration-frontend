/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import models.BackendAndFrontendJson._
import models.CompanyRegWrapper
import models.DataModel._
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DSTConnector  @Inject() (http: HttpClientV2, servicesConfig: ServicesConfig)(implicit executionContext: ExecutionContext) extends Logging {

  private val backendURL: String = servicesConfig.baseUrl("digital-services-tax") + "/digital-services-tax"

  def lookupCompany(utr: UTR, postcode: Postcode)(implicit hc:HeaderCarrier): Future[Option[CompanyRegWrapper]] = {
//    println(s"$utr utr, this is postcode $postcode")
//    println(s"$backendURL backendURL")
    http.get(url"$backendURL/lookup-company/$utr/$postcode").execute[Option[CompanyRegWrapper]]
  }
}
