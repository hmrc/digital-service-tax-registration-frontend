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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class UltimateParentCompanyNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey      = "ultimateParentCompanyName.error.required"
  val lengthKey        = "ultimateParentCompanyName.error.length"
  val invalidKey       = "ultimateParentCompanyName.error.invalid"
  val maxLength        = 105
  val companyNameRegex = """^[a-zA-Z0-9 '&.-]{1,105}$"""

  val form = new UltimateParentCompanyNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(companyNameRegex)
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{105}"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(companyNameRegex))
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
}
