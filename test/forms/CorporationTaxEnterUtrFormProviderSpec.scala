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

class CorporationTaxEnterUtrFormProviderSpec extends StringFieldBehaviours {

  val fieldName                   = "value"
  val requiredKey                 = "corporationTaxEnterUtr.error.required"
  val invalidKey                  = "corporationTaxEnterUtr.error.invalid"
  val uniqueTaxReferenceMaxRegex  = "^[0-9]{10}$"
  val utrRegexWithWhitespace      = """^\s*(\d\s*){10}$"""
  val maxLength                   = 10

  val form = new CorporationTaxEnterUtrFormProvider()()

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(uniqueTaxReferenceMaxRegex)
    )

    fieldThatBindsValidDataWithWhitespace(
      form,
      fieldName,
      RegexpGen.from(utrRegexWithWhitespace)
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{105}"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(uniqueTaxReferenceMaxRegex))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, invalidKey, Seq(uniqueTaxReferenceMaxRegex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
