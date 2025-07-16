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

import base.SpecBase
import controllers.routes
import pages._
import models._

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  val ukAddress: UkAddress =
    UkAddress("123 test street", None, None, None, "TE5 5ST")

  val internationalAddress: InternationalAddress = InternationalAddress(
    "123 Test Street",
    None,
    None,
    None,
    "AD"
  )

  val contactPersonName: ContactPersonName = ContactPersonName("John", "Smith")

  val companyName = "Big Corp"

  "Navigator" - {

    "in Normal mode" - {

      "must go to Journey Recovery Controller when user answers returns None" in {

        navigator.nextPage(CompanyNamePage, NormalMode, emptyUserAnswers) mustBe routes.JourneyRecoveryController
          .onPageLoad()
      }

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a GlobalRevenuesPage with option `false` to GlobalRevenuesNotEligible page" in {
        navigator.nextPage(
          GlobalRevenuesPage,
          NormalMode,
          UserAnswers("id")
            .set(GlobalRevenuesPage, false)
            .success
            .value
        ) mustBe routes.GlobalRevenuesNotEligibleController.onPageLoad()
      }

      "must go from a GlobalRevenuesPage with option `true` to UkRevenuesPage" in {
        navigator.nextPage(
          GlobalRevenuesPage,
          NormalMode,
          UserAnswers("id")
            .set(GlobalRevenuesPage, true)
            .success
            .value
        ) mustBe routes.UkRevenuesController.onPageLoad(NormalMode)
      }

      "must go from a UkRevenuesPage with option `false` to GlobalRevenuesNotEligible page" in {
        navigator.nextPage(
          UkRevenuesPage,
          NormalMode,
          UserAnswers("id")
            .set(UkRevenuesPage, false)
            .success
            .value
        ) mustBe routes.UkRevenueNotEligibleController.onPageLoad()
      }

      "must go from a UkRevenuesPage with option `true` to CheckCompanyRegisteredOfficeAddressPage" in {
        navigator.nextPage(
          UkRevenuesPage,
          NormalMode,
          UserAnswers("id")
            .set(UkRevenuesPage, true)
            .success
            .value
        ) mustBe routes.CheckCompanyRegisteredOfficeAddressController.onPageLoad(NormalMode)
      }

      "must go from ConfirmCompanyDetailsPage with option `true` with a Company Name and UK Office to ConfirmCompanyDetailsController" in {
        navigator.nextPage(
          ConfirmCompanyDetailsPage,
          NormalMode,
          UserAnswers("id")
            .set(ConfirmCompanyDetailsPage, true)
            .success
            .value
            .set(CompanyNamePage, companyName)
            .success
            .value
            .set(CompanyRegisteredOfficeUkAddressPage, ukAddress)
            .success
            .value
        ) mustBe routes.ConfirmCompanyDetailsController.onPageLoad(NormalMode)
      }

      "must go from ConfirmCompanyDetailsPage with option `true` with no Company Name to CompanyContactAddressController" in {
        navigator.nextPage(
          ConfirmCompanyDetailsPage,
          NormalMode,
          UserAnswers("id")
            .set(ConfirmCompanyDetailsPage, true)
            .success
            .value
            .set(CompanyRegisteredOfficeUkAddressPage, ukAddress)
            .success
            .value
        ) mustBe routes.CompanyContactAddressController.onPageLoad(NormalMode)
      }

      "must go from ConfirmCompanyDetailsPage with option `true` with no UK office to CompanyContactAddressController" in {
        navigator.nextPage(
          ConfirmCompanyDetailsPage,
          NormalMode,
          UserAnswers("id")
            .set(ConfirmCompanyDetailsPage, true)
            .success
            .value
            .set(CompanyNamePage, companyName)
            .success
            .value
        ) mustBe routes.CompanyContactAddressController.onPageLoad(NormalMode)
      }

      "must go from ConfirmCompanyDetailsPage with option `false` to Details Not Correct page" in {
        navigator.nextPage(
          ConfirmCompanyDetailsPage,
          NormalMode,
          UserAnswers("id")
            .set(ConfirmCompanyDetailsPage, false)
            .success
            .value
        ) mustBe routes.DetailsNotCorrectController.onPageLoad()
      }

      "must go from a CheckCompanyRegisteredOfficeAddressPage with option `true` to CheckCompanyRegisteredOfficePostcodePage" in {
        navigator.nextPage(
          CheckCompanyRegisteredOfficeAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckCompanyRegisteredOfficeAddressPage, true)
            .success
            .value
        ) mustBe routes.CheckCompanyOfficeRegisteredPostcodeController.onPageLoad(NormalMode)
      }

      "must go from a CheckCompanyRegisteredOfficeAddressPage with option `false` to CompanyNamePage" in {
        navigator.nextPage(
          CheckCompanyRegisteredOfficeAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckCompanyRegisteredOfficeAddressPage, false)
            .success
            .value
        ) mustBe routes.CompanyNameController.onPageLoad(NormalMode)
      }

      "must go from a CheckCompanyRegisteredOfficePostcodePage with valid postcode to CheckUtr page" in {
        navigator.nextPage(
          CheckCompanyRegisteredOfficePostcodePage,
          NormalMode,
          UserAnswers("id")
            .set(CheckCompanyRegisteredOfficePostcodePage, "SW3 5DA")
            .success
            .value
        ) mustBe routes.CheckUtrController.onPageLoad(NormalMode)
      }

      "must go from a checkUTR with option `true` to CorporationTaxEnterUtr page" in {
        navigator.nextPage(
          CheckUtrPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckUtrPage, true)
            .success
            .value
        ) mustBe routes.CorporationTaxEnterUtrController.onPageLoad(NormalMode)
      }

      "must go from a checkUTR with option 'false' to CompanyNamePage" in {
        navigator.nextPage(
          CheckUtrPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckUtrPage, false)
            .success
            .value
        ) mustBe routes.CompanyNameController.onPageLoad(NormalMode)
      }

      "must go from a CorporationTaxEnterUtr Page to Company Name page" in {
        navigator.nextPage(
          CorporationTaxEnterUtrPage,
          NormalMode,
          UserAnswers("id")
            .set(CorporationTaxEnterUtrPage, "1111110000")
            .success
            .value
        ) mustBe routes.CompanyNameController.onPageLoad(NormalMode)
      }

      "must got from CompanyNamePage to CompanyRegisteredOfficeUkAddress Page" in {
        navigator.nextPage(
          CompanyNamePage,
          NormalMode,
          UserAnswers("id")
            .set(CompanyNamePage, companyName)
            .success
            .value
        ) mustBe routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(NormalMode)

      }

      "must go from ContactUkAddress Page to GlobalRevenues page" in {
        navigator.nextPage(
          ContactUkAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(ContactUkAddressPage, ukAddress)
            .success
            .value
        ) mustBe routes.CheckIfGroupController.onPageLoad(NormalMode)
      }

      "must go from ContactInternationalAddress Page to CheckIfGroup page" in {
        navigator.nextPage(
          InternationalContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(InternationalContactAddressPage, internationalAddress)
            .success
            .value
        ) mustBe routes.CheckIfGroupController.onPageLoad(NormalMode)
      }

      "must go from a CheckContactAddressPage to contact-uk-address page" in {
        navigator.nextPage(
          CheckContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckContactAddressPage, true)
            .success
            .value
        ) mustBe routes.ContactUkAddressController.onPageLoad(NormalMode)
      }

      "must go from a CheckContactAddressPage to a contact-international-address page" in {
        navigator.nextPage(
          CheckContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckContactAddressPage, false)
            .success
            .value
        ) mustBe routes.InternationalContactAddressController.onPageLoad(NormalMode)
      }

      "must go from a CompanyContactAddressPage to a TODO-company-contact-address page" in pending

      "must go from CompanyContactAddress Page to CheckIfGroup page" in {
        navigator.nextPage(
          CompanyContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CompanyContactAddressPage, true)
            .success
            .value
        ) mustBe routes.CheckIfGroupController.onPageLoad(NormalMode)
      }

      "must go from CheckIfGroupPage to ultimate-parent-company-name page if answered `true`" in {
        navigator.nextPage(
          CheckIfGroupPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckIfGroupPage, true)
            .success
            .value
        ) mustBe routes.UltimateParentCompanyNameController.onPageLoad(NormalMode)
      }

      "must go from CheckIfGroupPage to ContactPersonName page if answer is false" in {
        navigator.nextPage(
          CheckIfGroupPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckIfGroupPage, false)
            .success
            .value
        ) mustBe routes.ContactPersonNameController.onPageLoad(NormalMode)
      }

      "must go from CompanyRegisteredOfficeAddressPage to CompanyContactAddressPage" in {
        navigator.nextPage(
          CompanyRegisteredOfficeUkAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(
              CompanyRegisteredOfficeUkAddressPage,
              UkAddress("kirby close", Some("12"), Some("london"), Some("essex"), "SW2 6IQ")
            )
            .success
            .value
        ) mustBe routes.CompanyContactAddressController.onPageLoad(NormalMode)
      }

      "must go from a CheckContactAddressPage to a TODO-contact-international-address page" in pending

      "must go from a CompanyContactAddress Page with option `true` to CheckIfGroup page" in {
        navigator.nextPage(
          CompanyContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(
              CompanyContactAddressPage,
              true
            )
            .success
            .value
        ) mustBe routes.CheckIfGroupController.onPageLoad(NormalMode)
      }

      "must go from a CompanyContactAddressPage with option `false` to CheckContactAddress page" in {
        navigator.nextPage(
          CompanyContactAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(CompanyContactAddressPage, false)
            .success
            .value
        ) mustBe routes.CheckContactAddressController.onPageLoad(NormalMode)
      }

      "must go from CheckIfGroupPage to a TODO contact-details page" in pending

      "must go from a UltimateParentCompanyNamePage to a check-ultimate-parent-company-address page" in {
        navigator.nextPage(
          UltimateParentCompanyNamePage,
          NormalMode,
          UserAnswers("id")
            .set(UltimateParentCompanyNamePage, companyName)
            .success
            .value
        ) mustBe routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(NormalMode)
      }

      "must go from a CheckUltimateGlobalParentCompanyInUkPage to a ultimate-parent-company-uk-address page" in {
        navigator.nextPage(
          CheckUltimateGlobalParentCompanyInUkPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckUltimateGlobalParentCompanyInUkPage, true)
            .success
            .value
        ) mustBe routes.UltimateParentCompanyUkAddressController.onPageLoad(NormalMode)
      }

      "must go from a CheckUltimateGlobalParentCompanyInUkPage to a ultimate-parent-company-international-address page" in {
        navigator.nextPage(
          CheckUltimateGlobalParentCompanyInUkPage,
          NormalMode,
          UserAnswers("id")
            .set(CheckUltimateGlobalParentCompanyInUkPage, false)
            .success
            .value
        ) mustBe routes.UltimateParentCompanyInternationalAddressController.onPageLoad(NormalMode)
      }

      "must go from a UltimateParentCompanyUkAddressPage to the ContactPersonName page" in {
        navigator.nextPage(
          UltimateParentCompanyUkAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(
              UltimateParentCompanyUkAddressPage,
              ukAddress
            )
            .success
            .value
        ) mustBe routes.ContactPersonNameController.onPageLoad(NormalMode)
      }

      "must go from a UltimateParentCompanyInternationalAddressController to the ContactPersonName page" in {
        navigator.nextPage(
          UltimateParentCompanyInternationalAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(
              UltimateParentCompanyInternationalAddressPage,
              internationalAddress
            )
            .success
            .value
        ) mustBe routes.ContactPersonNameController.onPageLoad(NormalMode)
      }

      "must go from a ContactPersonNameController to the ContactPersonPhoneNumber page" in {
        navigator.nextPage(
          ContactPersonNamePage,
          NormalMode,
          UserAnswers("id")
            .set(
              ContactPersonNamePage,
              contactPersonName
            )
            .success
            .value
        ) mustBe routes.ContactPersonPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go from a ContactPersonPhoneNumberController to the ContactPersonEmailAddress page" in {
        navigator.nextPage(
          ContactPersonPhoneNumberPage,
          NormalMode,
          UserAnswers("id")
            .set(
              ContactPersonNamePage,
              contactPersonName
            )
            .flatMap(ua => ua.set(ContactPersonPhoneNumberPage, "+447911123456"))
            .success
            .value
        ) mustBe routes.ContactPersonEmailAddressController.onPageLoad(NormalMode)
      }

      "must go from a ContactPersonEmailAddressController to the LiabilityStartDatePage page" in {
        navigator.nextPage(
          ContactPersonEmailAddressPage,
          NormalMode,
          UserAnswers("id")
            .set(
              ContactPersonNamePage,
              contactPersonName
            )
            .flatMap(ua => ua.set(ContactPersonEmailAddressPage, "johnsmith@gmail.com"))
            .success
            .value
        ) mustBe routes.LiabilityStartDateController.onPageLoad(NormalMode)
      }

      "must go from a LiabilityStartDatePage to the AccountingPeriodEndDatePage" in {
        navigator.nextPage(
          LiabilityStartDatePage,
          NormalMode,
          UserAnswers("id")
            .set(LiabilityStartDatePage, LocalDate.of(2022, 7, 7))
            .flatMap(ua => ua.set(CheckIfGroupPage, true))
            .success
            .value
        ) mustBe routes.AccountingPeriodEndDateController.onPageLoad(NormalMode)
      }

      "must go from a AccountingPeriodEndDate Page to the CheckYourAnswers Page" in {
        navigator.nextPage(
          AccountingPeriodEndDatePage,
          NormalMode,
          UserAnswers("id")
            .set(AccountingPeriodEndDatePage, LocalDate.of(2022, 7, 7))
            .success
            .value
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "must go from CheckIfGroup Page" - {

        "to UltimateParentCompanyNameController when user answers 'Yes'" in {

          navigator.nextPage(
            CheckIfGroupPage,
            CheckMode,
            emptyUserAnswers.set(CheckIfGroupPage, true).success.value
          ) mustBe routes.UltimateParentCompanyNameController.onPageLoad(CheckMode)
        }

        "to CheckYourAnswersController when user answers 'No'" in {

          navigator.nextPage(
            CheckIfGroupPage,
            CheckMode,
            emptyUserAnswers.set(CheckIfGroupPage, false).success.value
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "must go from UltimateParentCompanyName Page" - {

        "to CheckYourAnswers page when 'Ultimate Parent Company Is based in UK' question is set in User Answers" in {

          navigator.nextPage(
            UltimateParentCompanyNamePage,
            CheckMode,
            emptyUserAnswers
              .set(UltimateParentCompanyNamePage, companyName)
              .success
              .value
              .set(CheckUltimateGlobalParentCompanyInUkPage, true)
              .success
              .value
          ) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to 'Ultimate Parent Company Is based in UK' page when it is not set in User Answers" in {

          navigator.nextPage(
            UltimateParentCompanyNamePage,
            CheckMode,
            emptyUserAnswers
              .set(UltimateParentCompanyNamePage, companyName)
              .success
              .value
          ) mustBe routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(CheckMode)
        }
      }

      "must go from CheckUltimateGlobalParentCompanyInUk Page" - {

        "to CheckYourAnswers page" - {

          "when answer is 'Yes' and UK address is already set" in {

            navigator.nextPage(
              CheckUltimateGlobalParentCompanyInUkPage,
              CheckMode,
              emptyUserAnswers
                .set(CheckUltimateGlobalParentCompanyInUkPage, true)
                .success
                .value
                .set(UltimateParentCompanyUkAddressPage, ukAddress)
                .success
                .value
            ) mustBe routes.CheckYourAnswersController.onPageLoad()
          }

          "when answer is 'No' and International address is already set" in {

            navigator.nextPage(
              CheckUltimateGlobalParentCompanyInUkPage,
              CheckMode,
              emptyUserAnswers
                .set(CheckUltimateGlobalParentCompanyInUkPage, false)
                .success
                .value
                .set(UltimateParentCompanyInternationalAddressPage, internationalAddress)
                .success
                .value
            ) mustBe routes.CheckYourAnswersController.onPageLoad()
          }
        }

        "to UltimateParentCompanyUkAddress Page when answer is 'Yes' and UK address is not set" in {

          navigator.nextPage(
            CheckUltimateGlobalParentCompanyInUkPage,
            CheckMode,
            emptyUserAnswers
              .set(CheckUltimateGlobalParentCompanyInUkPage, true)
              .success
              .value
          ) mustBe routes.UltimateParentCompanyUkAddressController.onPageLoad(CheckMode)
        }

        "to UltimateParentCompanyInternationalAddress Page when answer is 'No' and International address is not set" in {

          navigator.nextPage(
            CheckUltimateGlobalParentCompanyInUkPage,
            CheckMode,
            emptyUserAnswers
              .set(CheckUltimateGlobalParentCompanyInUkPage, false)
              .success
              .value
          ) mustBe routes.UltimateParentCompanyInternationalAddressController.onPageLoad(CheckMode)
        }

        "back to the same page when User Answers is empty" in {

          navigator.nextPage(
            CheckUltimateGlobalParentCompanyInUkPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe routes.CheckUltimateGlobalParentCompanyInUkController.onPageLoad(CheckMode)
        }
      }
    }
  }
}
