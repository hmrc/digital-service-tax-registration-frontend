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

package navigation

import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import pages._
import play.api.mvc.Call

trait NavigationUtils {
  def globalRevenues(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(GlobalRevenuesPage).map {
      case true  => routes.UkRevenuesController.onPageLoad(NormalMode)
      case false => routes.GlobalRevenuesNotEligibleController.onPageLoad()
    }

  def ukRevenues(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(UkRevenuesPage).map {
      case true  => routes.CheckCompanyRegisteredOfficeAddressController.onPageLoad(NormalMode)
      case false => routes.UkRevenueNotEligibleController.onPageLoad()
    }

  def confirmCompanyDetailsPage(userAnswers: UserAnswers): Option[Call] = {
    val companyDetailsAreSet: Boolean =
      userAnswers.get(CompanyNamePage).isDefined && userAnswers.get(CompanyRegisteredOfficeUkAddressPage).isDefined

    userAnswers.get(ConfirmCompanyDetailsPage) map {
      case true if companyDetailsAreSet => routes.ConfirmCompanyDetailsController.onPageLoad(NormalMode)
      case true                         => routes.CompanyContactAddressController.onPageLoad(NormalMode)
      case false                        => routes.DetailsNotCorrectController.onPageLoad()
    }
  }

  def checkCompanyRegisteredOfficeAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckCompanyRegisteredOfficeAddressPage).map {
      case true  => routes.CheckCompanyOfficeRegisteredPostcodeController.onPageLoad(NormalMode)
      case false => routes.CompanyNameController.onPageLoad(NormalMode)
    }

  def checkCompanyRegisteredOfficePostcode(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckCompanyRegisteredOfficePostcodePage).map(_ => routes.CheckUtrController.onPageLoad(NormalMode))

  def checkUtr(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckUtrPage).map {
      case true  => routes.CorporationTaxEnterUtrController.onPageLoad(NormalMode)
      case false => routes.CompanyNameController.onPageLoad(NormalMode)
    }

  def corporationTaxEnterUtr(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CorporationTaxEnterUtrPage).map(_ => routes.CompanyNameController.onPageLoad(NormalMode))

  def companyNamePage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CompanyNamePage).map { _ =>
      routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(NormalMode)
    }

  def contactUkAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ContactUkAddressPage).map(_ => routes.GlobalRevenuesController.onPageLoad(NormalMode))

  def contactInternationalAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(InternationalContactAddressPage).map(_ => routes.CheckIfGroupController.onPageLoad(NormalMode))

  def companyRegisteredOfficeUkAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CompanyRegisteredOfficeUkAddressPage).map { _ =>
      routes.CompanyContactAddressController.onPageLoad(NormalMode)
    }

  def checkContactAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckContactAddressPage).map {
      case true  => routes.ContactUkAddressController.onPageLoad(NormalMode)
      case false => routes.InternationalContactAddressController.onPageLoad(NormalMode)
    }

  def checkIfGroup(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckIfGroupPage).map {
      case true  => routes.UltimateParentCompanyNameController.onPageLoad(NormalMode)
      case false => routes.ContactPersonNameController.onPageLoad(NormalMode)
    }

  def checkIfGroupCheckMode(userAnswers: UserAnswers): Call =
    if (userAnswers.get(CheckIfGroupPage).contains(true)) {
      routes.UltimateParentCompanyNameController.onPageLoad(CheckMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }

  def ultimateParentCompanyNamePage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(UltimateParentCompanyNamePage).map { _ =>
      routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(NormalMode)
    }

  def ultimateParentCompanyNamePageCheckMode(userAnswers: UserAnswers): Call =
    (userAnswers.get(UltimateParentCompanyNamePage), userAnswers.get(CheckUltimateGlobalParentCompanyInUkPage)) match {
      case (_, Some(_)) => routes.CheckYourAnswersController.onPageLoad()
      case (_, _)       => routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(CheckMode)
    }

  def companyContactAddress(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CompanyContactAddressPage).map {
      case true  => routes.CheckIfGroupController.onPageLoad(NormalMode)
      case false => routes.CheckContactAddressController.onPageLoad(NormalMode)
    }

  def ultimateParentCompanyUkAddressPage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(UltimateParentCompanyUkAddressPage).map { _ =>
      routes.ContactPersonNameController.onPageLoad(NormalMode)
    }

  def ultimateParentCompanyInternationalAddressPage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(UltimateParentCompanyInternationalAddressPage).map { _ =>
      routes.ContactPersonNameController.onPageLoad(NormalMode)
    }

  def checkUltimateGlobalParentCompanyInUkPage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(CheckUltimateGlobalParentCompanyInUkPage).map {
      case true  =>
        routes.UltimateParentCompanyUkAddressController.onPageLoad(NormalMode)
      case false =>
        routes.UltimateParentCompanyInternationalAddressController.onPageLoad(NormalMode)
    }

  def checkUltimateGlobalParentCompanyInUkPageCheckMode(userAnswers: UserAnswers): Call =
    userAnswers.get(CheckUltimateGlobalParentCompanyInUkPage) match {
      case Some(true)  =>
        if (userAnswers.get(UltimateParentCompanyUkAddressPage).isDefined) {
          routes.CheckYourAnswersController.onPageLoad()
        } else {
          routes.UltimateParentCompanyUkAddressController.onPageLoad(CheckMode)
        }
      case Some(false) =>
        if (userAnswers.get(UltimateParentCompanyInternationalAddressPage).isDefined) {
          routes.CheckYourAnswersController.onPageLoad()
        } else {
          routes.UltimateParentCompanyInternationalAddressController.onPageLoad(CheckMode)
        }
      case None        => routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(CheckMode)
    }

  def liabilityStartDatePage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(LiabilityStartDatePage).map { _ =>
      routes.AccountingPeriodEndDateController.onPageLoad(NormalMode)
    }

  def contactPersonNamePage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ContactPersonNamePage).map { _ =>
      routes.ContactPersonPhoneNumberController
        .onPageLoad(NormalMode)
    }

  def contactPersonPhoneNumberPage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ContactPersonPhoneNumberPage).map { _ =>
      routes.ContactPersonEmailAddressController
        .onPageLoad(NormalMode)
    }

  def contactPersonEmailAddressPage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ContactPersonEmailAddressPage).map { _ =>
      routes.LiabilityStartDateController
        .onPageLoad(NormalMode)
    }

  def accountingPeriodEndDatePage(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(AccountingPeriodEndDatePage).map(_ => routes.CheckYourAnswersController.onPageLoad())
}
