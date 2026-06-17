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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.InternationalContactAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class InternationalContactAddressSummarySpec extends SpecBase {

  implicit val messages: Messages = stubMessages()

  private val userAnswersId          = "id"
  private val mockLocation: Location = mock[Location]

  "InternationalContactAddressSummary" - {
    "row" - {
      "must return a row when the page contains an address" in {
        val addr        = InternationalAddress("123 Main Street", Some("Paris"), Some("Paris"), None, "FR")
        val userAnswers = UserAnswers(userAnswersId).set(InternationalContactAddressPage, addr).success.value

        when(mockLocation.name(ArgumentMatchers.eq(addr.countryCode))).thenReturn("France")
        val row = InternationalContactAddressSummary.row(userAnswers, mockLocation)
        row mustBe defined
      }

      "must return None when the page does not contain an address" in {
        val userAnswers = UserAnswers(userAnswersId)
        val row         = InternationalContactAddressSummary.row(userAnswers, mockLocation)
        row mustBe None
      }
    }
  }
}
