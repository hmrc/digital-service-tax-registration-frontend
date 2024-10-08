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
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class CompanyRegisteredOfficeUkAddressFormProviderSpec extends StringFieldBehaviours {
  val genAddress = RegexpGen.from("""^[a-zA-Z0-9'&.-]{1,30}$""".r.regex)

  val form = new CompanyRegisteredOfficeUkAddressFormProvider()()

  ".buildingorstreet" - {
    val fieldName   = "buildingorstreet"
    val requiredKey = "companyRegisteredOfficeUkAddress.error.buildingorstreet.required"
    val lengthKey   = "companyRegisteredOfficeUkAddress.error.buildingorstreet.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      genAddress
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".buildingorstreet2" - {

    val fieldName = "buildingorstreet2"
    val lengthKey = "companyRegisteredOfficeUkAddress.error.buildingorstreet2.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      genAddress
    )
    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }

  ".town" - {

    val fieldName = "town"
    val lengthKey = "companyRegisteredOfficeUkAddress.error.town.length"
    val maxLength = 35

    behave like fieldWithMaxLengthGeneratingFromRegex(
      form,
      fieldName,
      maxLength = maxLength,
      """^[a-zA-Z]$""",
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }

  ".county" - {

    val fieldName = "county"
    val lengthKey = "companyRegisteredOfficeUkAddress.error.county.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      genAddress
    )

    behave like fieldWithMaxLengthGeneratingFromRegex(
      form,
      fieldName,
      maxLength = maxLength,
      """^[a-zA-Z]$""",
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

  }
}
