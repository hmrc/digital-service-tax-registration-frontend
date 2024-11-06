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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import controllers.auth.routes
import models.InternalId
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthorisedAction])
trait Auth
    extends ActionRefiner[Request, AuthorisedRequest]
    with ActionBuilder[AuthorisedRequest, AnyContent]
    with AuthorisedFunctions {
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedRequest[A]]]
}

@Singleton
class AuthorisedAction @Inject() (
  mcc: MessagesControllerComponents,
  val authConnector: AuthConnector
)(implicit val appConfig: FrontendAppConfig, val executionContext: ExecutionContext, val messagesApi: MessagesApi)
    extends Auth {

  val logger: Logger                                                                                  = Logger(getClass)
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val retrieval = allEnrolments and credentialRole and internalId and affinityGroup

    authorised(AuthProviders(GovernmentGateway) and Organisation and User).retrieve(retrieval) { // CODE REFACTOR
      case enrolments ~ _ ~ id ~ _ =>
        val internalIdString = id.getOrElse(throw new RuntimeException("No Internal ID found for user"))
        val internalId       = InternalId(internalIdString) // REVISIT REGEX VALIDATED STRING

        Future.successful(Right(AuthorisedRequest(internalId, enrolments, request)))

    } recover {
      case _: UnsupportedAffinityGroup =>
        Left(Redirect(routes.IncorrectAccountAffinityController.onPageLoad()))

      case _: UnsupportedCredentialRole =>
        Left(Redirect(routes.IncorrectAccountCredRoleController.onPageLoad()))

      case _: NoActiveSession =>
        Left(Redirect(routes.AuthController.signOut()))
    }
  }

  override def parser: BodyParser[AnyContent] = mcc.parsers.anyContent

}

case class AuthorisedRequest[A](
  internalId: InternalId,
  enrolments: Enrolments,
  request: Request[A]
) extends WrappedRequest(request)
