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

import controllers.actions._
import forms.CorporationTaxEnterUtrFormProvider
import models.{CompanyRegWrapper, Mode, NormalMode, UkAddress, UserAnswers}
import navigation.Navigator
import pages._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DigitalServicesTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorporationTaxEnterUtrView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CorporationTaxEnterUtrController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  auth: Auth,
  formProvider: CorporationTaxEnterUtrFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorporationTaxEnterUtrView,
  service: DigitalServicesTaxService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (auth andThen identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(CorporationTaxEnterUtrPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (auth andThen identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            request.userAnswers.get(CheckCompanyRegisteredOfficePostcodePage) match {
              case None           =>
                Future
                  .successful(Redirect(routes.CheckCompanyOfficeRegisteredPostcodeController.onPageLoad(NormalMode)))
              case Some(postcode) =>
                for {
                  ua             <- Future.fromTry(request.userAnswers.set(CorporationTaxEnterUtrPage, value))
                  compRegWrapper <- service.lookupCompany(value, postcode)
                  updatedAnswers <- Future.fromTry(setCompanyAnswers(ua, compRegWrapper))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CorporationTaxEnterUtrPage, mode, updatedAnswers))
            }
        )
  }

  private def setCompanyAnswers(ua: UserAnswers, companyRegWrapper: Option[CompanyRegWrapper]): Try[UserAnswers] =
    companyRegWrapper match {
      case None             => Try(ua)
      case Some(companyReg) =>
        companyReg.company.address match {
          case ukAddress: UkAddress =>
            ua.set(CompanyNamePage, companyReg.company.name)
              .flatMap(_.set(CompanyRegisteredOfficeUkAddressPage, ukAddress))
              .flatMap(_.set(CheckCompanyRegisteredOfficeAddressPage, true))
          case _                    => Try(ua)
        }
    }
}
