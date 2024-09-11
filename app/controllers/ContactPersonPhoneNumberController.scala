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

import controllers.actions._
import controllers.routes
import forms.ContactPersonPhoneNumberFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.{ContactPersonNamePage, ContactPersonPhoneNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactPersonPhoneNumberView

import scala.concurrent.{ExecutionContext, Future}

class ContactPersonPhoneNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ContactPersonPhoneNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ContactPersonPhoneNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ContactPersonPhoneNumberPage).fold(form)(form.fill)
      getNameOrRedirect { name =>
        Ok(view(preparedForm, name, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            getNameOrRedirect(name => BadRequest(view(formWithErrors, name, mode)))
          ),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactPersonPhoneNumberPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ContactPersonPhoneNumberPage, mode, updatedAnswers))
      )
  }

  private def getNameOrRedirect(block: String => Result)(implicit request: DataRequest[AnyContent]): Result = {
    request.userAnswers.get(ContactPersonNamePage) match {
      case Some(contactName) => block(contactName.fullName)
      case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
