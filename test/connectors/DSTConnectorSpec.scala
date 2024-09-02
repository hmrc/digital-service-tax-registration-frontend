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

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.ModelGenerators
import models.BackendAndFrontendJson._
import models.CompanyRegWrapper
import models.DataModel._
import org.scalacheck.Arbitrary
import org.scalatest.OptionValues
import org.scalatest.concurrent._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

class DSTConnectorSpec extends AnyFreeSpec
  with ScalaFutures
  with IntegrationPatience
  with ModelGenerators
  with OptionValues
  with WireMockSupport{

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.digital-services-tax.port" -> wireMockPort
    )
    .build()

  lazy val connector: DSTConnector = app.injector.instanceOf[DSTConnector]

  "DSTConnectorSpec" - {

    "lookupCompany" - {

      "should lookup a company successfully by utr and postcode" in {
        forAll { (utr: UTR, postcode: Postcode, reg: CompanyRegWrapper) =>
          val escaped = postcode.replaceAll("\\s+", "")

          stubGet(Json.toJson(reg), s"/digital-services-tax/lookup-company/$utr/$escaped", OK)

          println(Json.toJson(reg))

          val response = connector.lookupCompany(utr, postcode)
          whenReady(response) { res =>
            res mustBe defined
            res.value mustEqual reg
          }
        }
      }

      "should return none for an invalid input" in {
        val wrapper = Arbitrary.arbitrary[CompanyRegWrapper].sample.value
        val utr = Arbitrary.arbitrary[UTR].sample.value
        val postcode = Arbitrary.arbitrary[Postcode].sample.value
        val escaped = postcode.replaceAll("\\s+", "")

        stubGet(Json.toJson(wrapper), s"/digital-services-tax/lookup-company/$utr/$escaped", NOT_FOUND)

        val response = connector.lookupCompany(utr, postcode)
        whenReady(response) { res =>
          res mustBe None
        }
      }
    }

  }


  private def stubGet(body: JsValue, url: String, status: Int): Any =
    wireMockServer.stubFor(
      get(urlPathEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body.toString())
        )
    )
}
