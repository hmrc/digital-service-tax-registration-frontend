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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{CompanyNamePage, ContactPersonEmailAddressPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{CheckYourAnswersService, DigitalServicesTaxService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersService: CheckYourAnswersService,
  service: DigitalServicesTaxService,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      summaryLists            <- checkYourAnswersService.getSummaryForView
      childCompanyName        <- checkYourAnswersService.getChildCompanyName
      parentCompanyName       <- checkYourAnswersService.getParentCompanyName
      isRegistrationCompleted <- checkYourAnswersService.isRegistrationCompleted
    } yield (summaryLists, childCompanyName, isRegistrationCompleted) match {
      case (Some(list), Some(childCompany), Some(isRegistrationCompleted)) =>
        if (!isRegistrationCompleted) {
          Ok(view(list, childCompany, parentCompanyName))
        } else {
          Redirect(routes.RegistrationController.registrationComplete)
        }
      case _                                                               =>
        // $COVERAGE-OFF$
        logger.warn("Failed to retrieve answers from cache, redirecting to journey recovery")
        // $COVERAGE-ON$
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    def redirect: Result = Redirect(routes.RegistrationController.registerAction)

    checkYourAnswersService.buildRegistration
      .flatMap {
        _.fold(Future.successful(redirect)) {
          service.submitRegistration(_) map {
            case s if s.status == OK =>
              (request.userAnswers.get(CompanyNamePage), request.userAnswers.get(ContactPersonEmailAddressPage)) match {
                case (Some(companyName), Some(contactPersonEmailAddressPage)) =>
                  Redirect(routes.RegistrationController.registrationSent(companyName, contactPersonEmailAddressPage))
                case _                                                        =>
                  // $COVERAGE-OFF$
                  logger.warn("Failed to retrieve answers from cache, redirecting to application complete anyway")
                  // $COVERAGE-ON$
                  Redirect(routes.RegistrationController.registrationComplete)
              }
            case _                   => redirect
          }
        }
      }
  }
}
