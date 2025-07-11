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

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Value
import viewmodels.govuk.summarylist.ValueViewModel

trait SummaryFunctions {

  def asAddressValue(lines: Seq[String]): Value =
    ValueViewModel(
      HtmlContent(
        "<ul class=\"govuk-list\">" + lines.map { line =>
          s"""<li>${HtmlFormat.escape(line)}<li>"""
        }.mkString + "</ul>"
      )
    )
}
