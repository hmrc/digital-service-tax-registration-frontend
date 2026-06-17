/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{UserAnswers, UkAddress}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.TryValues
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import pages.UltimateParentCompanyUkAddressPage

class UltimateParentCompanyUkAddressSummarySpec extends AnyFreeSpec with Matchers with TryValues {

  implicit val messages: Messages = stubMessages()

  private val userAnswersId = "id"

  "UltimateParentCompanyUkAddressSummary" - {
    "row" - {
      "must return a row when the page contains an address" in {
        val addr = UkAddress("10 Downing Street", Some("London"), Some("London"), None, "SW1A 2AA")
        val userAnswers = UserAnswers(userAnswersId).set(UltimateParentCompanyUkAddressPage, addr).success.value
        val row = UltimateParentCompanyUkAddressSummary.row(userAnswers)
        row mustBe defined
      }

      "must return None when the page does not contain an address" in {
        val userAnswers = UserAnswers(userAnswersId)
        val row = UltimateParentCompanyUkAddressSummary.row(userAnswers)
        row mustBe None
      }
    }
  }
}
