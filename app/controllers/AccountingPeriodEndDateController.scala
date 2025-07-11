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

package controllers

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.AccountingPeriodEndDateFormProvider
import models.Mode
import navigation.Navigator
import pages.{AccountingPeriodEndDatePage, CheckIfGroupPage, LiabilityStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AccountingPeriodEndDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountingPeriodEndDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  val formProvider: AccountingPeriodEndDateFormProvider,
  view: AccountingPeriodEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(CheckIfGroupPage)
      .flatMap { isGroup =>
        request.userAnswers.get(LiabilityStartDatePage).map { liabilityStartDate =>
          val form = formProvider(isGroup, liabilityStartDate)
          view(request.userAnswers.get(AccountingPeriodEndDatePage).fold(form)(ap => form.fill(ap)), mode, isGroup)
        }
      }
      .fold(Redirect(routes.JourneyRecoveryController.onPageLoad()))(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(CheckIfGroupPage)
        .flatMap { isGroup =>
          request.userAnswers.get(LiabilityStartDatePage).map { liabilityStartDate =>
            formProvider(isGroup, liabilityStartDate)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isGroup))),
                accountingPeriodEndDate =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(request.userAnswers.set(AccountingPeriodEndDatePage, accountingPeriodEndDate))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(AccountingPeriodEndDatePage, mode, updatedAnswers))
              )
          }
        }
        .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
  }
}
