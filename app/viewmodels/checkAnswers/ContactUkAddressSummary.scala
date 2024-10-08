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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ContactUkAddressPage
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ContactUkAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ContactUkAddressPage).map { answer =>
      SummaryListRowViewModel(
        key = "contactUkAddress.checkYourAnswersLabel",
        value = ValueViewModel(
          HtmlContent(
            Html(
              s"${HtmlFormat.escape(answer.buildingOrStreet).toString}<br>" +
                answer.buildingOrStreetLine2
                  .map(value => s"${HtmlFormat.escape(value).body}<br>")
                  .getOrElse("") +
                answer.townOrCity
                  .map(value => s"${HtmlFormat.escape(value).body}<br>")
                  .getOrElse("") +
                answer.county
                  .map(value => s"${HtmlFormat.escape(value).body}<br>")
                  .getOrElse("") +
                HtmlFormat
                  .escape(
                    answer.postcode
                  )
                  .toString()
            )
          )
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.ContactUkAddressController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("contactUkAddress.change.hidden"))
        )
      )
    }
}
