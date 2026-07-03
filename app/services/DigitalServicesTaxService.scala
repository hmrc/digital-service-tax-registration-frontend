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

package services

import connectors.DigitalServicesTaxConnector
import models.{Company, CompanyRegWrapper, Registration, RegistrationJourneyState}
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DigitalServicesTaxService @Inject() (backendConnector: DigitalServicesTaxConnector) {

  def getCompany(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Company]] =
    lookupCompany.map(_.map(_.company))

  def lookupCompany(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[CompanyRegWrapper]] =
    backendConnector.lookupCompany

  def lookupCompany(utr: String, postcode: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[CompanyRegWrapper]] =
    backendConnector.lookupCompany(utr, postcode)

  def lookupPendingRegistrationExists(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    backendConnector.lookupPendingRegistrationExists.map {
      case resp if resp.status == OK => true
      case _                         => false
    }

  def lookupRegistration(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Registration]] =
    backendConnector.lookupRegistration

  def getRegistrationJourneyState(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[RegistrationJourneyState] =
    for {
      pendingRegistrationExists <- lookupPendingRegistrationExists
      registration              <- lookupRegistration
    } yield (pendingRegistrationExists, registration) match {
      case (true, _)                                              => RegistrationJourneyState.Pending
      case (false, Some(reg)) if reg.registrationNumber.isDefined => RegistrationJourneyState.Existing
      case (false, Some(_))                                       => RegistrationJourneyState.Pending
      case _                                                      => RegistrationJourneyState.New
    }

  def submitRegistration(
    registration: Registration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    backendConnector.submitRegistration(registration)
}
