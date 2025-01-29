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

package models

import base.SpecBase
import pages.{ContactPersonEmailAddressPage, ContactPersonNamePage, ContactPersonPhoneNumberPage}
import queries.Settable

class ContactDetailsSpec extends SpecBase {

  "ContactDetails" - {

    "when .getFromUserAnswers is called" - {

      val specParams = Seq[(String, Settable[_])](
        ("name", ContactPersonNamePage),
        ("phone number", ContactPersonPhoneNumberPage),
        ("email address", ContactPersonEmailAddressPage)
      )

      val contactPerson = ContactPersonName("John", "Smith")
      val phoneNumber = "071234567890"
      val email = "test@test.com"

      val completeUserAnswers = UserAnswers("id")
        .set(ContactPersonNamePage, contactPerson).success.value
        .set(ContactPersonPhoneNumberPage, phoneNumber).success.value
        .set(ContactPersonEmailAddressPage, email).success.value

      "must return None when" - {

        specParams foreach { x =>

          s"${x._1} is missing from User Answers" in {
            val userAnswers = completeUserAnswers.remove(x._2).success.value
            assert(ContactDetails.getFromUserAnswers(userAnswers).isEmpty)
          }
        }
      }

      "must return Contact Details when all fields are set in User Answers" in {

        val result = ContactDetails.getFromUserAnswers(completeUserAnswers).value

        result.forename mustBe contactPerson.firstName
        result.surname mustBe contactPerson.lastName
        result.phoneNumber mustBe phoneNumber
        result.email mustBe email
      }
    }
  }
}
