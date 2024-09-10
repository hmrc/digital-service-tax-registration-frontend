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
import forms.mappings.Constraints
import play.api.data.FormError
import org.scalacheck.Arbitrary.arbitrary
import wolfendale.scalacheck.regexp.RegexpGen

class ContactUkAddressFormProviderSpec extends StringFieldBehaviours {

  val form             = new ContactUkAddressFormProvider()()
  val addressLineRegex = """^[A-Za-z0-9 \-,.&']*$"""
  val validData        = """^[A-Za-z0-9-,.&']{1, 35}*$"""
  val maxLength        = 35

  ".Building or street" - {

    val fieldName   = "building-or-street"
    val requiredKey = "contactUkAddress.error.buildingOrStreet.required"
    val lengthKey   = "contactUkAddress.error.buildingOrStreet.length"
    val invalidKey  = "contactUkAddress.error.buildingOrStreet.invalid"

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

  ".Building or street line 2" - {

    val fieldName  = "building-or-street-line-2"
    val lengthKey  = "contactUkAddress.error.buildingOrStreetLine2.length"
    val invalidKey = "contactUkAddress.error.buildingOrStreetLine2.invalid"

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
  }

  ".Twon or City" - {

    val fieldName  = "town-or-city"
    val lengthKey  = "contactUkAddress.error.townOrCity.length"
    val invalidKey = "contactUkAddress.error.townOrCity.invalid"

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
  }

  ".County" - {

    val fieldName  = "county"
    val lengthKey  = "contactUkAddress.error.county.length"
    val invalidKey = "contactUkAddress.error.county.invalid"

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
  }

  ".Postcode" - {

    val fieldName   = "postcode"
    val requiredKey = "contactUkAddress.error.postcode.required"
    val invalidKey  = "error.invalid.postcode"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      genPostcode
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegexpWithGenerator(
      form,
      fieldName,
      regexp = Constraints.postcodeRegex.toString(),
      generator = arbitrary[String],
      error = FormError(fieldName, invalidKey)
    )
  }
}
