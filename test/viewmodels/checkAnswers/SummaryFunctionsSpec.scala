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

import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import scala.language.postfixOps

class SummaryFunctionsSpec extends AnyFreeSpec with Matchers with TryValues {

  implicit val messages: Messages = stubMessages()

  private val summaryFunctions = new SummaryFunctions {}

  "SummaryFunctions" - {
    "asAddressValue" - {
      "must format result" in {
        val lines = Seq("10 Downing Street", "London", "SW1A 2AA")
        val value = summaryFunctions.asAddressValue(lines)
        value.content.asHtml.toString mustBe "<ul class=\"govuk-list\"><li>10 Downing Street<li><li>London<li><li>SW1A 2AA<li></ul>"
      }

      "must handle empty address lines" in {
        val lines = Seq()
        val value = summaryFunctions.asAddressValue(lines)
        value.content.asHtml.toString mustBe "<ul class=\"govuk-list\"></ul>"
      }
    }
  }
}
