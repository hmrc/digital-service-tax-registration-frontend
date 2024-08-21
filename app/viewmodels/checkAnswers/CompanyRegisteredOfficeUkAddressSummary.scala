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
import pages.CompanyRegisteredOfficeUkAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CompanyRegisteredOfficeUkAddressSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyRegisteredOfficeUkAddressPage).map {
      answer =>

      val buldingvalue2 = answer.buildingorstreet2.getOrElse("")
      val buildingToRender = if(!buldingvalue2.isEmpty) HtmlFormat.escape(buldingvalue2).toString + "<br/>" else ""

      val townValue = answer.town.getOrElse("")
      val town = if(!townValue.isEmpty) HtmlFormat.escape(townValue).toString + "<br/>" else ""

      val countyValue = answer.town.getOrElse("")
      val county = if(!countyValue.isEmpty) HtmlFormat.escape(townValue).toString + "<br/>" else ""


      val value = HtmlFormat.escape(answer.buildingorstreet).toString + "<br/>" + buildingToRender + town +county + HtmlFormat.escape(answer.postcode).toString

        SummaryListRowViewModel(
          key     = "companyRegisteredOfficeUkAddress.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("companyRegisteredOfficeUkAddress.change.hidden"))
          )
        )
    }
}
