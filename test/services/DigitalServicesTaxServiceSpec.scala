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

package services

import base.SpecBase
import connectors.DigitalServicesTaxConnector
import generators.ModelGenerators
import models.CompanyRegWrapper
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class DigitalServicesTaxServiceSpec
    extends SpecBase
    with MockitoSugar
    with ScalaCheckDrivenPropertyChecks
    with ModelGenerators {

  val mockConnector: DigitalServicesTaxConnector = mock[DigitalServicesTaxConnector]
  val mockWrapper: CompanyRegWrapper             = mock[CompanyRegWrapper]

  val serviceUnderTest = new DigitalServicesTaxService(mockConnector)

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "DigitalServicesTaxService" - {

    "when .lookupCompany is called" - {

      "must return an instance of CompanyRegWrapper when the connector returns Some" in {

        forAll(genCompanyRegWrapper) { wrapper =>
          when(mockConnector.lookupCompany(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(wrapper)))

          serviceUnderTest.lookupCompany.futureValue mustBe Some(wrapper)
        }
      }

      "must return None when the connect returns None" in {

        when(mockConnector.lookupCompany(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        serviceUnderTest.lookupCompany.futureValue mustBe None
      }
    }

    "when .lookupCompany with UTR and Postcode is called" - {

      "must return an instance of CompanyRegWrapper when the connect returns Some" in {

        forAll(genPostcode, genCompanyRegWrapper.suchThat(_.utr.nonEmpty)) { (postcode, wrapper) =>
          val utr = wrapper.utr.value

          when(mockConnector.lookupCompany(eqTo(utr), eqTo(postcode))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(wrapper)))

          serviceUnderTest.lookupCompany(utr, postcode).futureValue mustBe Some(wrapper)
        }
      }

      "must return None when the connect returns None" in {

        when(mockConnector.lookupCompany(any[String], any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        serviceUnderTest.lookupCompany("111111111", "TE35ST").futureValue mustBe None
      }
    }

    "when .getCompany is called" - {

      "must return an instance of Company when the connect returns Some" in {

        forAll(genCompany) { company =>
          when(mockConnector.lookupCompany(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(CompanyRegWrapper(company, None, None))))

          serviceUnderTest.getCompany.futureValue mustBe Some(company)
        }
      }

      "must return None when the connect returns None" in {

        when(mockConnector.lookupCompany(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        serviceUnderTest.getCompany.futureValue mustBe None
      }
    }

    "when .submitRegistration is called" - {

      "must return the given HttpResponse object" - {

        "when result is OK" in {

          forAll(genRegistration) { reg =>
            val response = HttpResponse(OK, "Success")

            when(mockConnector.submitRegistration(eqTo(reg))(any(), any()))
              .thenReturn(Future.successful(response))

            serviceUnderTest.submitRegistration(reg).futureValue mustBe response
          }
        }

        "when result is a non-200 status code" in {

          forAll(genRegistration, Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE)) {
            (reg, status) =>
              val response = HttpResponse(status, "Error")

              when(mockConnector.submitRegistration(eqTo(reg))(any(), any()))
                .thenReturn(Future.successful(response))

              serviceUnderTest.submitRegistration(reg).futureValue mustBe response
          }
        }
      }
    }
  }
}
