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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, urlEqualTo}
import models.{Company, CompanyRegWrapper, UkAddress}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext

class DigitalServicesTaxConnectorISpec
  extends AnyFreeSpec
    with Matchers
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "DigitalServicesTaxConnector" - {

    "when .lookupCompany is called" - {

      val url = "/digital-services-tax/lookup-company"

      def app: Application =
        new GuiceApplicationBuilder()
          .configure("microservice.services.digital-services-tax.port" -> server.port)
          .build()

      lazy val connector: DigitalServicesTaxConnector = app.injector.instanceOf[DigitalServicesTaxConnector]

      "must return expected object when 200 is received" in {

        val wrapper = CompanyRegWrapper(
          Company(
            "Big Corp",
            UkAddress("123 Test Street", Some("Business Park"), Some("Long Road"), Some("London"), "XX1 1XX")
          ),
          Some("1111111111"),
          Some("12415GFEWSDG"),
          useSafeId = true
        )

        server.stubFor(
          WireMock.get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(Json.toJson(wrapper).toString())
            )
        )

        connector.lookupCompany.futureValue mustBe Some(wrapper)
      }

      "must return None when 404 is received" in {

        server.stubFor(
          WireMock.get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(NOT_FOUND)
            )
        )

        connector.lookupCompany.futureValue mustBe None
      }

      "must throw when 500 is received" in {

        server.stubFor(
          WireMock.get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(INTERNAL_SERVER_ERROR)
            )
        )

        an[Exception] mustBe thrownBy { // TODO get working with UpstreamErrorResponse
          connector.lookupCompany.futureValue
        }
      }
    }
  }
}
