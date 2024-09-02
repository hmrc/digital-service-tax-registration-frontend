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

import controllers.routes
import models.{NormalMode, UserAnswers}
import pages.{CheckCompanyRegisteredOfficeAddressPage, CheckCompanyRegisteredOfficePostcodePage, CheckContactAddressPage, CheckIfGroupPage, CheckUltimateGlobalParentCompanyInUkPage, CheckUtrPage, CompanyContactAddressPage, CompanyNamePage, CompanyRegisteredOfficeUkAddressPage, ContactUkAddressPage, CorporationTaxEnterUtrPage, GlobalRevenuesPage, UkRevenuesPage, UltimateParentCompanyNamePage, UltimateParentCompanyUkAddressPage}
import play.api.mvc.Call

trait NavigationUtils {
  def globalRevenues(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(GlobalRevenuesPage).map {
      case true => routes.UkRevenuesController.onPageLoad(NormalMode)
      case false => routes.GlobalRevenuesNotEligibleController.onPageLoad()
    }
  }

  def ukRevenues(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(UkRevenuesPage).map {
      case true => routes.CheckCompanyRegisteredOfficeAddressController.onPageLoad(NormalMode)
      case false => routes.UkRevenueNotEligibleController.onPageLoad()
    }
  }

  def checkCompanyRegisteredOfficeAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckCompanyRegisteredOfficeAddressPage).map {
      case true => routes.CheckCompanyOfficeRegisteredPostcodeController.onPageLoad(NormalMode)
      case false => routes.CompanyNameController.onPageLoad(NormalMode)
    }
  }

  def checkCompanyRegisteredOfficePostcode(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckCompanyRegisteredOfficePostcodePage).map(_ => routes.CheckUtrController.onPageLoad(NormalMode))
  }

  def checkUtr(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckUtrPage).map {
      case true => routes.CorporationTaxEnterUtrController.onPageLoad(NormalMode)
      case false => routes.CompanyNameController.onPageLoad(NormalMode)
    }
  }

  def corporationTaxEnterUtr(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CorporationTaxEnterUtrPage).map { _ => routes.CompanyNameController.onPageLoad(NormalMode)}
  }

  def companyNamePage(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CompanyNamePage).map { _ => routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(NormalMode) }
  }

  def contactUkAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(ContactUkAddressPage).map { _ => routes.GlobalRevenuesController.onPageLoad(NormalMode) }
  }

  def companyRegisteredOfficeUkAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CompanyRegisteredOfficeUkAddressPage).map { _ => routes.CompanyContactAddressController.onPageLoad(NormalMode) }
  }

  def checkContactAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckContactAddressPage).map {
      case true => routes.ContactUkAddressController.onPageLoad(NormalMode)
      case false => routes.InternationalContactAddressController.onPageLoad(NormalMode)
    }
  }

  def checkIfGroup(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckIfGroupPage).map {
      case true => routes.UltimateParentCompanyNameController.onPageLoad(NormalMode)
      case false => ??? // todo page needs to be implemented contact-details
    }
  }

  def ultimateParentCompanyNamePage(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(UltimateParentCompanyNamePage).map { _ => routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(NormalMode)}
  }

  def companyContactAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CompanyContactAddressPage).map {
      case true => routes.CheckIfGroupController.onPageLoad(NormalMode) // TODO page needs to  be implemented
      case false => ??? // TODO page needs to  be implemented
    }
  }

  def ultimateParentCompanyUkAddresPage(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(UltimateParentCompanyUkAddressPage).map { _ => routes.GlobalRevenuesController.onPageLoad(NormalMode)}
  }


  def checkUltimateGlobalParentCompanyInUkPage(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(CheckUltimateGlobalParentCompanyInUkPage).map {
      case true => routes.UltimateParentCompanyUkAddressController.onPageLoad(NormalMode) // TODO page needs to  be implemented
      case false => routes.UltimateParentCompanyInternationalAddressController.onPageLoad(NormalMode) // TODO page needs to  be implemented
    }
  }

}