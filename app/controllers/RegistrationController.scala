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

package controllers

import controllers.actions.{Auth, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{CompanyNamePage, ContactPersonEmailAddressPage, RegistrationCompletePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{RegistrationCompleteView, RegistrationPendingView, RegistrationSentView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  auth: Auth,
  val controllerComponents: MessagesControllerComponents,
  registrationCompleteView: RegistrationCompleteView,
  registrationSentView: RegistrationSentView,
  registrationPendingView: RegistrationPendingView,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def registerAction(): Action[AnyContent] = (auth andThen identify andThen getData andThen requireData) {
    Redirect(routes.JourneyRecoveryController.onPageLoad())
  }

  def registrationSent(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(RegistrationCompletePage, true))
        _              <- sessionRepository.set(updatedAnswers)
      } yield (request.userAnswers.get(CompanyNamePage), request.userAnswers.get(ContactPersonEmailAddressPage)) match {
        case (Some(companyName), Some(contactPersonEmailAddress)) =>
          Ok(registrationSentView(companyName, contactPersonEmailAddress))
        case _                                                    =>
          Redirect(routes.RegistrationController.registrationComplete())
      }
    }

  def registrationComplete(): Action[AnyContent] = (identify andThen getData) { implicit request =>
    sessionRepository.clear(request.userId)
    Ok(registrationCompleteView())
  }

  def registrationPending(): Action[AnyContent] = (identify andThen getData) { implicit request =>
    sessionRepository.clear(request.userId)
    Ok(registrationPendingView())
  }
}
