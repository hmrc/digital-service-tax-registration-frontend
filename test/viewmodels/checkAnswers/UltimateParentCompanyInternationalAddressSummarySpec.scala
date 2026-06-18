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

import base.SpecBase
import models.{InternationalAddress, Location, UserAnswers}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.UltimateParentCompanyInternationalAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class UltimateParentCompanyInternationalAddressSummarySpec extends SpecBase {

  implicit val messages: Messages = stubMessages()

  private val userAnswersId = "id"

  "UltimateParentCompanyInternationalAddressSummary" - {
    "row" - {
      "must return a row when the page contains an address" in {
        val mockLocation: Location = mock[Location]
        val addr                   = InternationalAddress("123 Main Street", Some("Paris"), Some("Paris"), None, "FR")
        val userAnswers            =
          UserAnswers(userAnswersId).set(UltimateParentCompanyInternationalAddressPage, addr).success.value
        when(mockLocation.name(eqTo("FR"))).thenReturn("France")
        val row                    = UltimateParentCompanyInternationalAddressSummary.row(userAnswers, mockLocation)
        row mustBe defined
      }

      "must return None when the page does not contain an address" in {
        val userAnswers            = UserAnswers(userAnswersId)
        val mockLocation: Location = mock[Location]
        val row                    = UltimateParentCompanyInternationalAddressSummary.row(userAnswers, mockLocation)
        row mustBe None
      }
    }
  }
}
