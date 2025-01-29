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
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import generators.ModelGenerators
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class DigitalServicesTaxConnectorISpec
    extends AnyFreeSpec
    with Matchers
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with ScalaCheckDrivenPropertyChecks
    with ModelGenerators {

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

        forAll(genCompanyRegWrapper) { wrapper =>

          server.stubFor(
            WireMock.get(url)
              .willReturn(
                ok(Json.toJson(wrapper).toString())
              )
          )

          connector.lookupCompany.futureValue mustBe Some(wrapper)
        }
      }

      "must return None when 404 is received" in {

        server.stubFor(
          WireMock.get(url)
            .willReturn(
              notFound()
            )
        )

        connector.lookupCompany.futureValue mustBe None
      }

      "must throw when 500 is received" in {

        server.stubFor(
          WireMock.get(url)
            .willReturn(
              serverError()
            )
        )

        an[Exception] mustBe thrownBy {
          connector.lookupCompany.futureValue
        }
      }
    }

    "when .lookupCompany with UTR and Postcode is called" - {

      val utr = "1111111111"
      val postcode = "TE35ST"
      val url = s"/digital-services-tax/lookup-company/$utr/$postcode"

      def app: Application =
        new GuiceApplicationBuilder()
          .configure("microservice.services.digital-services-tax.port" -> server.port)
          .build()

      lazy val connector: DigitalServicesTaxConnector = app.injector.instanceOf[DigitalServicesTaxConnector]

      "must return expected object when 200 is received" in {

        forAll(genCompanyRegWrapper) { wrapper =>
          server.stubFor(
            WireMock.get(url)
              .willReturn(
                ok(Json.toJson(wrapper).toString())
              )
          )

          connector.lookupCompany(utr, postcode).futureValue mustBe Some(wrapper)
        }
      }

      "must return None when 404 is received" in {

        server.stubFor(
          WireMock.get(url)
            .willReturn(
              notFound()
            )
        )

        connector.lookupCompany(utr, postcode).futureValue mustBe None
      }

      "must throw when 500 is received" in {

        server.stubFor(
          WireMock.get(url)
            .willReturn(
              serverError()
            )
        )

        an[Exception] mustBe thrownBy {
          connector.lookupCompany(utr, postcode).futureValue
        }
      }
    }

    "when .submitRegistration is called" - {

      val url = "/digital-services-tax/registration"

      def app: Application =
        new GuiceApplicationBuilder()
          .configure("microservice.services.digital-services-tax.port" -> server.port)
          .build()

      lazy val connector: DigitalServicesTaxConnector = app.injector.instanceOf[DigitalServicesTaxConnector]

      "must handle a successful response" in {

        forAll(genRegistration) { reg =>
          server.stubFor(
            WireMock.post(url)
              .willReturn(
                ok("{}")
              )
          )

          connector.submitRegistration(reg).futureValue.status mustBe OK
        }
      }

      "must throw" - {

        "in the case of a failed response" in {

          forAll(genRegistration) { reg =>
            server.stubFor(
              WireMock.post(url)
                .willReturn(
                  badRequest()
                    .withBody("{}")
                )
            )

            an[Exception] mustBe thrownBy {
              connector.submitRegistration(reg).futureValue
            }
          }
        }

        "in the case of an error" in {

          forAll(genRegistration) { reg =>
            server.stubFor(
              WireMock.post(url)
                .willReturn(
                  aResponse()
                    .withFault(Fault.MALFORMED_RESPONSE_CHUNK)
                )
            )

            an[Exception] mustBe thrownBy {
              connector.submitRegistration(reg).futureValue
            }
          }
        }
      }
    }
  }
}
