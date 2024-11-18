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

package controllers

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.CheckContactAddressFormProvider
import models.requests.DataRequest
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{CheckContactAddressPage, CompanyRegisteredOfficeUkAddressPage, ContactUkAddressPage, InternationalContactAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckContactAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  formProvider: CheckContactAddressFormProvider,
  view: CheckContactAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(CheckContactAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              contactUserAnswers <- Future.fromTry(handleUserAnswer(value))
              updatedAnswers     <- Future.fromTry(contactUserAnswers.set(CheckContactAddressPage, value))
              _                  <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CheckContactAddressPage, mode, updatedAnswers))
        )
  }

  private def handleUserAnswer(answer: Boolean)(implicit request: DataRequest[AnyContent]): Try[UserAnswers] =
    if (answer) {
      (
        request.userAnswers.get(CompanyRegisteredOfficeUkAddressPage),
        request.userAnswers.get(InternationalContactAddressPage)
      ) match { // TODO get International address here when implemented
        case (Some(addr), _) => request.userAnswers.set(ContactUkAddressPage, addr.ToContactUKAddress)
        case (_, Some(addr)) => Try(request.userAnswers) // TODO set International address here when implemented
        case (_, _)          => Try(request.userAnswers)
      }
    } else {
      Try(request.userAnswers)
    }
}
