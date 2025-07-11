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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

class UltimateParentCompanyNameFormProvider @Inject() extends Mappings {

  val maxLength        = 105
  val companyNameRegex = """^[a-zA-Z0-9 '&.-]{1,105}$"""

  private def validCompanyName: Constraint[String] =
    Constraint("constraints.companyName") { str =>
      if (str.length > maxLength) {
        Invalid(ValidationError("ultimateParentCompanyName.error.length", maxLength))
      } else if (!str.matches(companyNameRegex)) {
        Invalid(ValidationError("ultimateParentCompanyName.error.invalid", companyNameRegex))
      } else {
        Valid
      }
    }

  def apply(): Form[String] =
    Form(
      "value" -> text("ultimateParentCompanyName.error.required")
        .verifying(validCompanyName)
    )
}
