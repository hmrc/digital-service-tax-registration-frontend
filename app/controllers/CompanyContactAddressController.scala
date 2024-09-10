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
import forms.CompanyContactAddressFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.{CompanyContactAddressPage, CompanyRegisteredOfficeUkAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CompanyContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyContactAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CompanyContactAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyContactAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(CompanyContactAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    renderPage(mode, preparedForm, Ok)
  }

  private def renderPage(mode: Mode, form: Form[Boolean], status: Status)(implicit request: DataRequest[AnyContent]) =
    request.userAnswers.get(CompanyRegisteredOfficeUkAddressPage) match {
      case Some(address) => status(view(form, address, mode))
      case _             => Redirect(routes.JourneyRecoveryController.onPageLoad())
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(renderPage(mode, formWithErrors, BadRequest)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CompanyContactAddressPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CompanyContactAddressPage, mode, updatedAnswers))
        )
  }
}
