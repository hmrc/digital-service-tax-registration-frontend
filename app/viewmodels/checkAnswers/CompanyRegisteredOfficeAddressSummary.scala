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
import models.{CheckMode, Location, UserAnswers}
import pages.{CheckCompanyRegisteredOfficeAddressPage, CompanyRegisteredOfficeInternationalAddressPage, CompanyRegisteredOfficeUkAddressPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CompanyRegisteredOfficeAddressSummary extends SummaryFunctions {

  def row(answers: UserAnswers, location: Location)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CheckCompanyRegisteredOfficeAddressPage) match {
      case Some(true)  => getCompanyRegisteredOfficeUKAddressRow(answers)
      case Some(false) => getCompanyRegisteredOfficeInternationalAddressRow(answers, location)
      case _           => None
    }

  private def getCompanyRegisteredOfficeUKAddressRow(
    answers: UserAnswers
  )(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyRegisteredOfficeUkAddressPage).map { answer =>
      SummaryListRowViewModel(
        key = "companyRegisteredOfficeUkAddress.checkYourAnswersLabel",
        value = asAddressValue(answer.asAddressLines),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("companyRegisteredOfficeUkAddress.change.hidden"))
        )
      )
    }

  private def getCompanyRegisteredOfficeInternationalAddressRow(answers: UserAnswers, location: Location)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(CompanyRegisteredOfficeInternationalAddressPage).map { answer =>
      SummaryListRowViewModel(
        key = "companyRegisteredOfficeInternationalAddress.checkYourAnswersLabel",
        value = asAddressValue(answer.asAddressLines(location)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.CompanyRegisteredOfficeInternationalAddressController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("companyRegisteredOfficeInternationalAddress.change.hidden"))
        )
      )
    }
}
