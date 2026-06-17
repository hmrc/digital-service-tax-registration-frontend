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

import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import org.scalatest.TryValues
import pages.CheckIfGroupPage

class CheckIfGroupSummarySpec extends AnyFreeSpec with Matchers with TryValues {

  implicit val messages: Messages = stubMessages()

  private val userAnswersId = "id"

  "CheckIfGroupSummary" - {
    "row" - {
      "must return a row when the answer is true" in {
        val userAnswers = UserAnswers(userAnswersId).set(CheckIfGroupPage, true).success.value
        val row = CheckIfGroupSummary.row(userAnswers)
        row mustBe defined
      }

      "must return a row when the answer is false" in {
        val userAnswers = UserAnswers(userAnswersId).set(CheckIfGroupPage, false).success.value
        val row = CheckIfGroupSummary.row(userAnswers)
        row mustBe defined
      }

      "must return None when the page does not contain an answer" in {
        val userAnswers = UserAnswers(userAnswersId)
        val row = CheckIfGroupSummary.row(userAnswers)
        row mustBe None
      }
    }
  }
}
