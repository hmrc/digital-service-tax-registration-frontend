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

import base.SpecBase
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

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

      /**
       * TODO the /company-name page needs to be done
        */
      "must go from a CheckCompanyRegisteredOfficeAddressPage with option `false` to TODO page" in pending

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

      "must go from a checkUTR with option `true` to TODO page" in pending
      
      "must go from a checkUTR with option `false` to TODO page" in pending

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
