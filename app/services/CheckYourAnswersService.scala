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

package services

import models.requests.DataRequest
import models.{Location, Registration, UserAnswers}
import pages.{CompanyNamePage, UltimateParentCompanyNamePage}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import queries.Gettable
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersService @Inject() (
  digitalServicesTaxService: DigitalServicesTaxService,
  sessionRepository: SessionRepository,
  location: Location
)(implicit
  ec: ExecutionContext
) {

  def getChildCompanyName(implicit request: DataRequest[_]): Future[Option[String]] =
    retrieveFromUserAnswers(CompanyNamePage)

  def getParentCompanyName(implicit request: DataRequest[_]): Future[Option[String]] =
    retrieveFromUserAnswers(UltimateParentCompanyNamePage)

  def getSummaryForView(implicit
    request: DataRequest[_],
    messages: Messages
  ): Future[Option[Map[String, SummaryList]]] =
    sessionRepository.get(request.userId) map { userAnswersOpt =>
      userAnswersOpt map { userAnswers =>
        Map(
          "responsibleMember"       -> Some(getResponsibleMemberDetailsList(userAnswers)),
          "ultimateGlobalParent"    -> getUltimateGlobalParentCompanyDetailsList(userAnswers),
          "contactPersonDetails"    -> Some(getContactPersonDetailsList(userAnswers)),
          "accountingPeriodDetails" -> Some(getAccountingPeriodDetailsList(userAnswers))
        ) collect { case (key, Some(value)) =>
          (key, value)
        }
      }
    }

  def buildRegistration(implicit request: DataRequest[_], hc: HeaderCarrier): Future[Option[Registration]] =
    sessionRepository.get(request.userId) flatMap { userAnswersOpt =>
      userAnswersOpt.fold(Future.successful[Option[Registration]](None)) { ua =>
        Registration.fromUserAnswers(ua, digitalServicesTaxService)
      }
    }

  private def getResponsibleMemberDetailsList(userAnswers: UserAnswers)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      Seq(
        CompanyNameSummary.row(userAnswers),
        CompanyRegisteredOfficeUkAddressSummary.row(userAnswers),
        ContactUkAddressSummary.row(userAnswers),
        InternationalContactAddressSummary.row(userAnswers, location),
        CheckIfGroupSummary.row(userAnswers)
      ).flatten
    )

  private def getUltimateGlobalParentCompanyDetailsList(
    userAnswers: UserAnswers
  )(implicit messages: Messages): Option[SummaryList] = {
    val rows = Seq(
      UltimateParentCompanyNameSummary.row(userAnswers),
      UltimateParentCompanyUkAddressSummary.row(userAnswers),
      UltimateParentCompanyInternationalAddressSummary.row(userAnswers, location)
    ).flatten

    if (rows.nonEmpty) {
      Some(SummaryListViewModel(rows))
    } else {
      None
    }
  }

  private def getContactPersonDetailsList(userAnswers: UserAnswers)(implicit messages: Messages) =
    SummaryListViewModel(
      Seq(
        ContactPersonNameSummary.row(userAnswers),
        ContactPersonPhoneNumberSummary.row(userAnswers),
        ContactPersonEmailAddressSummary.row(userAnswers)
      ).flatten
    )

  private def getAccountingPeriodDetailsList(userAnswers: UserAnswers)(implicit messages: Messages) =
    SummaryListViewModel(
      Seq(
        LiabilityStartDateSummary.row(userAnswers),
        AccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    )

  private def retrieveFromUserAnswers[A](
    a: Gettable[A]
  )(implicit request: DataRequest[_], reads: Reads[A]): Future[Option[A]] =
    sessionRepository.get(request.userId).map(_.flatMap(_.get[A](a)))
}
