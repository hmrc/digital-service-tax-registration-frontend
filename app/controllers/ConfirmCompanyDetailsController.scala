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

import connectors.DSTConnector
import controllers.actions._
import forms.ConfirmCompanyDetailsFormProvider
import models.DataModel.{Postcode, UTR}

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{CheckCompanyRegisteredOfficePostcodePage, ConfirmCompanyDetailsPage, CorporationTaxEnterUtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmCompanyDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         dstConnector: DSTConnector,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ConfirmCompanyDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ConfirmCompanyDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(ConfirmCompanyDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val result = request.userAnswers.get(CorporationTaxEnterUtrPage) match {
        case Some(utr) => request.userAnswers.get(CheckCompanyRegisteredOfficePostcodePage) match {
          case Some(postcode) => dstConnector.lookupCompany(UTR(utr), Postcode(postcode))
          case None => Future.successful(None)
        }
        case None => Future.successful(None)
      }

      for {
        optCompanyRegWrapper <- result
      } yield {
        val companyRegWrapper = optCompanyRegWrapper.get
        Ok(view(preparedForm, companyRegWrapper.company.address, mode))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val result = request.userAnswers.get(CorporationTaxEnterUtrPage) match {
        case Some(utr) => request.userAnswers.get(CheckCompanyRegisteredOfficePostcodePage) match {
          case Some(postcode) => dstConnector.lookupCompany(UTR(utr), Postcode(postcode))
          case None => Future.successful(None)
        }
        case None => Future.successful(None)
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, , mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmCompanyDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ConfirmCompanyDetailsPage, mode, updatedAnswers))
      )
  }
}
