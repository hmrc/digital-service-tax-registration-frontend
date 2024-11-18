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

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.UkRevenuesFormProvider
import models.{Company, Mode, UserAnswers}
import navigation.Navigator
import pages.{CompanyNamePage, CompanyRegisteredOfficeUkAddressPage, UkRevenuesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DigitalServicesTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UkRevenuesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UkRevenuesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: UkRevenuesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  backendService: DigitalServicesTaxService,
  view: UkRevenuesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(UkRevenuesPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

          val answersWithCompanyIfExists =
            if (value) {
              for {
                companyOpt              <- backendService.getCompany
                answersWithCompanyIfSet <- Future.fromTry(setCompanyAnswers(userAnswers, companyOpt))
              } yield answersWithCompanyIfSet
            } else {
              Future.successful(userAnswers)
            }

          for {
            answers        <- answersWithCompanyIfExists
            revenueAnswers <- Future.fromTry(answers.set(UkRevenuesPage, value))
            _              <- sessionRepository.set(revenueAnswers)
          } yield Redirect(navigator.nextPage(UkRevenuesPage, mode, revenueAnswers))
        }
      )
  }

  private def setCompanyAnswers(ua: UserAnswers, companyOpt: Option[Company]): Try[UserAnswers] =
    companyOpt.fold(Try(ua)) { company =>
      ua.set(CompanyNamePage, company.name)
        .flatMap(_.set(CompanyRegisteredOfficeUkAddressPage, company.address.toCompanyRegisteredOfficeUkAddress))
    }
}
