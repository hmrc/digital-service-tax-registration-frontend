/*
 * Copyright 2026 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.requests.IdentifierRequest
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Organisation and User).retrieve(internalId) { internalId =>
      val internalIdString = internalId.getOrElse(throw new RuntimeException("No Internal ID found for user"))

      if (config.dstNewRegistrationFrontendEnableFlag) {
        block(IdentifierRequest(request, internalIdString))
      } else {
        Future.successful(Redirect(config.dstFrontendRegistrationUrl))
      }
    } recover {
      case _: NoActiveSession           =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl), "accountType" -> Seq("Organisation")))
      case _: UnsupportedAffinityGroup  =>
        Redirect(controllers.auth.routes.IncorrectAccountAffinityController.onPageLoad())
      case _: UnsupportedCredentialRole =>
        Redirect(controllers.auth.routes.IncorrectAccountCredRoleController.onPageLoad())
      case _: AuthorisationException    =>
        Redirect(controllers.routes.UnauthorisedController.onPageLoad())
    }
  }
}
