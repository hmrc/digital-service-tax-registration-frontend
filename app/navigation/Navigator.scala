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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() extends NavigationUtils {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case GlobalRevenuesPage => ua => globalRevenues(ua)
    case UkRevenuesPage => ua => ukRevenues(ua)
    case CheckCompanyRegisteredOfficeAddressPage => ua => checkCompanyRegisteredOfficeAddress(ua)
    case CheckCompanyRegisteredOfficePostcodePage => ua => checkCompanyRegisteredOfficePostcode(ua)
    case CheckUtrPage => ua => checkUtr(ua)
    case CompanyNamePage => ua => companyNamePage(ua)
    case CorporationTaxEnterUtrPage => ua => corporationTaxEnterUtr(ua)
    case CheckContactAddressPage => ua => checkContactAddress(ua)
    case CompanyContactAddressPage => ua => companyContactAddressPage(ua)
    case ContactUkAddressPage => ua => contactUkAddress(ua)
    case CompanyRegisteredOfficeUkAddressPage => ua => companyRegisteredOfficeUkAddress(ua)
    case CheckIfGroupPage => ua => checkIfGroup(ua)
    case UltimateParentCompanyNamePage => ua => ultimateParentCompanyNamePage(ua)
    case _ => _ => Some(routes.IndexController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
