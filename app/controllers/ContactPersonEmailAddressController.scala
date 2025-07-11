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
import forms.ContactPersonEmailAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.{ContactPersonEmailAddressPage, ContactPersonNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactPersonEmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactPersonEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ContactPersonEmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContactPersonEmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(ContactPersonEmailAddressPage).fold(form)(form.fill)
    request.userAnswers
      .get(ContactPersonNamePage)
      .fold(Redirect(routes.JourneyRecoveryController.onPageLoad()))(name =>
        Ok(view(preparedForm, name.fullName, mode))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val formWithData = form.bindFromRequest()

      if (formWithData.hasErrors) {
        Future.successful {
          request.userAnswers
            .get(ContactPersonNamePage)
            .fold(Redirect(routes.JourneyRecoveryController.onPageLoad()))(name =>
              BadRequest(view(formWithData, name.fullName, mode))
            )
        }
      } else {
        formWithData.value.fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { email =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactPersonEmailAddressPage, email))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ContactPersonEmailAddressPage, mode, updatedAnswers))
        }
      }
  }
}
