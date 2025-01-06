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
import forms.InternationalContactAddressFormProvider
import models.requests.DataRequest
import models.{InternationalAddress, Location, Mode}
import navigation.Navigator
import pages.{UltimateParentCompanyInternationalAddressPage, UltimateParentCompanyNamePage, UltimateParentCompanyUkAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UltimateParentCompanyInternationalAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UltimateParentCompanyInternationalAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  location: Location,
  formProvider: InternationalContactAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UltimateParentCompanyInternationalAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[InternationalAddress] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(UltimateParentCompanyInternationalAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    renderPage(mode, preparedForm, Ok)
  }

  private def renderPage(mode: Mode, form: Form[InternationalAddress], status: Status)(implicit
    request: DataRequest[AnyContent]
  ): Result =
    request.userAnswers.get(UltimateParentCompanyNamePage) match {
      case Some(parentName) =>
        status(view(form, location.countrySelectList(form.data, location.countryListWithoutGB), parentName, mode))
      case _                => Redirect(routes.JourneyRecoveryController.onPageLoad())
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(renderPage(mode, formWithErrors, BadRequest)),
          value =>
            for {
              answersWithoutUKAddress <- Future.fromTry(
                                           request.userAnswers.removeIfSet(UltimateParentCompanyUkAddressPage)
                                         )
              updatedAnswers          <-
                Future.fromTry(answersWithoutUKAddress.set(UltimateParentCompanyInternationalAddressPage, value))
              _                       <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UltimateParentCompanyInternationalAddressPage, mode, updatedAnswers))
        )
  }
}
