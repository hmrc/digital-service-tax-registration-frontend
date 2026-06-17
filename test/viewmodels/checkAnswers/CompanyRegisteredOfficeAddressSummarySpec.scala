/*
 * Copyright 2026 HM Revenue & Customs
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

package viewmodels.checkAnswers

import models.{InternationalAddress, Location, UkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{CheckCompanyRegisteredOfficeAddressPage, CompanyRegisteredOfficeInternationalAddressPage, CompanyRegisteredOfficeUkAddressPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CompanyRegisteredOfficeAddressSummarySpec extends AnyFreeSpec with Matchers with TryValues {

  implicit val messages: Messages = stubMessages()

  private val userAnswersId = "id"

  "CompanyRegisteredOfficeAddressSummary" - {
    "row" - {
      "must return a row when the page contains UK address data" in {
        val addr = UkAddress("10 Downing Street", Some("London"), Some("London"), None, "SW1A 2AA")
        val userAnswers = UserAnswers(userAnswersId)
          .set(CheckCompanyRegisteredOfficeAddressPage, true).success.value
          .set(CompanyRegisteredOfficeUkAddressPage, addr).success.value
        val row = CompanyRegisteredOfficeAddressSummary.row(userAnswers, null)
        row mustBe defined
      }

      "must return a row when the page contains International address data" in {
        val addr = InternationalAddress("10 White house", Some("Line 2"), Some("Line 3"), Some("Line 3"), "FR")
        val mockLocation: Location = mock[Location]
        when(mockLocation.name(eqTo("FR"))).thenReturn("France")
        val userAnswers = UserAnswers(userAnswersId)
          .set(CheckCompanyRegisteredOfficeAddressPage, false).success.value
          .set(CompanyRegisteredOfficeInternationalAddressPage, addr).success.value
        
        
        val row = CompanyRegisteredOfficeAddressSummary.row(userAnswers, mockLocation)
        row mustBe defined
      }

      "must return None when the page does not contain an answer" in {
        val userAnswers = UserAnswers(userAnswersId)
        val row = CompanyRegisteredOfficeAddressSummary.row(userAnswers, null)
        row mustBe None
      }
    }
  }
}
