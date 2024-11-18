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
import forms.ConfirmCompanyDetailsFormProvider
import models.requests.DataRequest
import models.{Company, Location, Mode}
import navigation.Navigator
import pages.{CompanyNamePage, CompanyRegisteredOfficeUkAddressPage, ConfirmCompanyDetailsPage, UkRevenuesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmCompanyDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConfirmCompanyDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  location: Location,
  view: ConfirmCompanyDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(ConfirmCompanyDetailsPage).fold(form)(form.fill)

    renderViewOrRedirectIfNoCompany(mode, company => Ok(view(company, location, preparedForm, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful {
              renderViewOrRedirectIfNoCompany(
                mode,
                company => BadRequest(view(company, location, formWithErrors, mode))
              )
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmCompanyDetailsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ConfirmCompanyDetailsPage, mode, updatedAnswers))
        )
  }

  private def renderViewOrRedirectIfNoCompany(mode: Mode, f: Company => Result)(implicit
    request: DataRequest[AnyContent]
  ): Result =
    (request.userAnswers.get(CompanyNamePage), request.userAnswers.get(CompanyRegisteredOfficeUkAddressPage)) match {
      case (Some(name), Some(address)) =>
        f(Company(name, address.ToUKAddress)) // TODO change model to standardised Address
      case _                           => Redirect(navigator.nextPage(UkRevenuesPage, mode, request.userAnswers))
    }
}
