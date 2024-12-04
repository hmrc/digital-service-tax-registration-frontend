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

package forms.behaviours

import forms.mappings.Constraints
import forms.mappings.Constraints.Address
import models.{InternationalAddress, Location, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait AddressFieldBehaviours extends StringFieldBehaviours {

  def requiredKey(prefix: String, field: String) = s"$prefix.error.$field.required"
  def lengthKey(prefix: String, field: String)   = s"$prefix.error.$field.length"
  def invalidKey(prefix: String, field: String)  = s"$prefix.error.$field.invalid"

  val maxLength = 35

  val addressLineGen: Gen[String] = RegexpGen.from(Address.addressRegex).suchThat(x => x.trim.nonEmpty && x.length <= maxLength)

  def ukAddressFields(form: Form[UkAddress]): Unit = {

    val prefix = "ukAddress"

    testMandatoryAddressLine(form, prefix)

    Seq("line2", "line3", "line4") foreach { fieldName =>
      testOptionalAddressLine(form, fieldName, prefix)
    }

    testPostcodeField(form, prefix)
  }

  def internationalAddressFields(form: Form[InternationalAddress], location: Location): Unit = {

    val prefix = "internationalAddress"

    testMandatoryAddressLine(form, prefix)

    Seq("line2", "line3", "line4") foreach { fieldName =>
      testOptionalAddressLine(form, fieldName, prefix)
    }

    testCountryField(form, location)
  }

  private def testMandatoryAddressLine(form: Form[_], prefix: String): Unit = {

    val fieldName = "line1"

    s".$fieldName" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        addressLineGen
      )

      behave like fieldWithInValidData(
        form,
        fieldName,
        RegexpGen.from(s"[!£^*()]"),
        invalidDataError = FormError(fieldName, invalidKey(prefix, fieldName), Seq(Address.addressRegex))
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey(prefix, fieldName), Seq(maxLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey(prefix, fieldName))
      )
    }
  }

  private def testOptionalAddressLine(form: Form[_], fieldName: String, prefix: String): Unit = {

    s".$fieldName must" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        addressLineGen
      )

      behave like fieldWithInValidData(
        form,
        fieldName,
        RegexpGen.from(s"[!£^*()]"),
        invalidDataError = FormError(fieldName, invalidKey(prefix, fieldName), Seq(Address.addressRegex))
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey(prefix, fieldName), Seq(maxLength))
      )
    }
  }

  private def testPostcodeField(form: Form[UkAddress], prefix: String): Unit = {

    val fieldName = "postcode"

    s".$fieldName" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        genPostcode
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey(prefix, fieldName))
      )

      behave like fieldWithRegexpWithGenerator(
        form,
        fieldName,
        regexp = Constraints.postcodeRegex,
        generator = arbitrary[String],
        error = FormError(fieldName, invalidKey(prefix, fieldName), Seq(Constraints.postcodeRegex))
      )
    }
  }

  private def testCountryField(form: Form[InternationalAddress], location: Location): Unit = {

    ".country" - {

      val fieldName   = "country"

      val formError = FormError(fieldName, requiredKey("internationalAddress", fieldName))

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        Gen.oneOf(location.countryListWithoutGB.map(_.code))
      )

      behave like mandatoryField(
        form,
        fieldName,
        formError
      )

      behave like fieldWithInValidData(
        form,
        fieldName,
        genCountryCode.suchThat(code => !location.countryListWithoutGB.map(_.code).contains(code)),
        formError
      )

      s"not bind when given GB" in {
        val result = form.bind(Map(fieldName -> "GB")).apply(fieldName)
        result.errors must contain only formError
      }
    }
  }
}
