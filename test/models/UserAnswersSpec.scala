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

package models

import base.SpecBase
import pages._

class UserAnswersSpec extends SpecBase {

  "UserAnswers" - {

    "when .removeUltimateParentCompanyAnswers is called" - {

      "must remove the correct answers from data" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(CheckIfGroupPage, true)
          .success
          .value
          .set(UltimateParentCompanyNamePage, "Parent Company")
          .success
          .value
          .set(CheckUltimateGlobalParentCompanyInUkPage, false)
          .success
          .value
          .set(
            UltimateParentCompanyUkAddressPage,
            UltimateParentCompanyUkAddress(
              "123 Test Road",
              Some("Big District"),
              Some("Newcastle"),
              Some("Tyne and Wear"),
              "NE9 1QQ"
            )
          )
          .success
          .value
          .set(
            UltimateParentCompanyInternationalAddressPage,
            InternationalAddress(
              "678 Big Street",
              Some("Brooklyn"),
              Some("Manhattan"),
              Some("New York"),
              "US"
            )
          )
          .success
          .value

        val result = userAnswers.removeUltimateParentCompanyAnswers.success.value

        assert(result.get(CheckIfGroupPage).isDefined)
        assert(result.get(UltimateParentCompanyNamePage).isEmpty)
        assert(result.get(CheckUltimateGlobalParentCompanyInUkPage).isEmpty)
        assert(result.get(UltimateParentCompanyUkAddressPage).isEmpty)
        assert(result.get(UltimateParentCompanyInternationalAddressPage).isEmpty)
      }
    }

    "when .removeIfSet is called" - {

      "must not change the state of User Answers if value is not set" in {

        val userAnswers = UserAnswers(userAnswersId)
        userAnswers.removeIfSet(CheckIfGroupPage).success.value mustBe userAnswers
      }

      "must remove value from User Answers if it is present" in {

        val updatedUserAnswers = emptyUserAnswers.set(CheckIfGroupPage, true).success.value
        updatedUserAnswers.removeIfSet(CheckIfGroupPage).success.value.data mustBe emptyUserAnswers.data
      }
    }
  }
}