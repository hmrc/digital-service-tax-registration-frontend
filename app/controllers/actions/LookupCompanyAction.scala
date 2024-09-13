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

package controllers.actions

import connectors.DSTConnector
import controllers.actions.LookupCompanyAction.LookupCompanyDataRequest
import controllers.routes
import models.DataModel.{Postcode, UTR}
import models.requests.DataRequest
import models.{CompanyRegWrapper, UserAnswers}
import pages.{CheckCompanyRegisteredOfficePostcodePage, CorporationTaxEnterUtrPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Request, Result, WrappedRequest}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupCompanyActionImpl @Inject()(dstConnector: DSTConnector)
                                   (implicit val executionContext: ExecutionContext) extends LookupCompanyAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, LookupCompanyDataRequest[A]]] = {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    (for {
      utr <- request.userAnswers.get(CorporationTaxEnterUtrPage)
      postcode <- request.userAnswers.get(CheckCompanyRegisteredOfficePostcodePage)
    } yield {
      dstConnector.lookupCompany(UTR(utr), Postcode(postcode)).map {
        case Some(companyRegWrapper) => Right(LookupCompanyDataRequest(request, request.userId, request.userAnswers, companyRegWrapper))
        case None => Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }).getOrElse(Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))))
  }
}

object LookupCompanyAction {
  case class LookupCompanyDataRequest[A](request: Request[A],
                                         userId: String,
                                         userAnswers: UserAnswers,
                                         companyRegWrapper: CompanyRegWrapper) extends WrappedRequest[A](request)
}

trait LookupCompanyAction extends ActionRefiner[DataRequest, LookupCompanyDataRequest]

