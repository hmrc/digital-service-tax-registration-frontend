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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CheckYourAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersService: CheckYourAnswersService,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      summaryLists      <- checkYourAnswersService.getSummaryForView
      childCompanyName  <- checkYourAnswersService.getChildCompanyName
      parentCompanyName <- checkYourAnswersService.getParentCompanyName
    } yield (summaryLists, childCompanyName) match {
      case (Some(list), Some(childCompany)) =>
        Ok(view(list, childCompany, parentCompanyName))
      case (None, Some(_))                  =>
        logger.warn("Could not retrieve summary lists from User Answers")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      case (Some(_), None)                  =>
        logger.warn("Could not retrieve child company name from User Answers")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit() = (identify andThen getData andThen requireData) { implicit request =>
    /*
     * TODO:
     * - Implement submit registration
     * - Check existing frontend for model and connector setup
     */

    ???
  }
}
