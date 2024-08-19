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
import models.Country
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class InternationalAddressFormProviderSpec extends StringFieldBehaviours {
  val locations: Seq[Country] = Seq(Country("Andorra", "AD", "country"))
  val form = new InternationalAddressFormProvider()(locations)
  val addressLineRegex = """^[A-Za-z0-9 \-,.&']*$"""
  val validData = """^[A-Za-z0-9-,.&']{1, 35}*$"""

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "internationalAddress.error.line1.required"
    val lengthKey = "internationalAddress.error.line1.length"
    val invalidKey = "internationalAddress.error.line1.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(validData)
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*()]"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(addressLineRegex))
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

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "internationalAddress.error.line2.length"
    val invalidKey = "internationalAddress.error.line2.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(validData)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*()]"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(addressLineRegex))
    )
  }

  ".line3" - {

    val fieldName = "line3"
    val lengthKey = "internationalAddress.error.line3.length"
    val invalidKey = "internationalAddress.error.line3.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(validData)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*()]"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(addressLineRegex))
    )
  }

  ".line4" - {

    val fieldName = "line4"
    val lengthKey = "internationalAddress.error.line4.length"
    val invalidKey = "internationalAddress.error.line4.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(validData)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*()]"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(addressLineRegex))
    )
  }

  ".country" - {

    val fieldName = "country"
    val requiredKey = "internationalAddress.error.countryCode.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      "AD"
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

  }
}
