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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.{CheckIfGroupPage, LiabilityStartDatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LiabilityStartDateSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    //todo - work in progress DDCYLS-9232
    val msg = {
      if (answers.get(CheckIfGroupPage).getOrElse(false)) "group" else "company"
    }

    answers.get(LiabilityStartDatePage).map { answer =>
      SummaryListRowViewModel(
        key = "liabilityStartDate.checkYourAnswersLabel",
        value = ValueViewModel(answer.format(dateTimeFormat()(using messages.lang))),
        actions = Seq(
          ActionItemViewModel("site.change", routes.LiabilityStartDateController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("liabilityStartDate.change.hidden", msg))
        )
      )
    }
  }
}
