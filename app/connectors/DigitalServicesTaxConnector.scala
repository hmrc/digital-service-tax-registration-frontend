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

import config.FrontendAppConfig
import models.CompanyRegWrapper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DigitalServicesTaxConnector @Inject() (http: HttpClientV2, appConfig: FrontendAppConfig) {

  private def backendUrl = s"${appConfig.dstBackendBaseUrl}/digital-services-tax"

  def lookupCompany(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[CompanyRegWrapper]] = {
    val url: URL = url"$backendUrl/lookup-company"
    http.get(url).execute[Option[CompanyRegWrapper]]
  }
}