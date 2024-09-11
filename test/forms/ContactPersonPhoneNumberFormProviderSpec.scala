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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen.numChar
import play.api.data.FormError

class ContactPersonPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  private val maxLength  = 24
  private val phoneRegex = "^[0-9 \\-]{1,24}$"

  val form = new ContactPersonPhoneNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength, numChar)
    )

    behave like fieldWithMaxLengthGeneratingFromRegex(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, "contactPersonPhoneNumber.error.length", Seq(maxLength)),
      regex = phoneRegex
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "contactPersonPhoneNumber.error.required")
    )

    behave like fieldWithRegexpWithGenerator(
      form,
      fieldName,
      phoneRegex,
      stringsExceptSpecificValues(Seq(0 to 9).map(_.toString)).suchThat(_.length <= maxLength),
      FormError(fieldName, "contactPersonPhoneNumber.error.invalid", Seq(phoneRegex))
    )
  }
}
