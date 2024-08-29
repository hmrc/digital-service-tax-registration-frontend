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

import forms.mappings.Constraints.CompanyName

import javax.inject.Inject
import forms.mappings.{Constraints, Mappings}
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

class CompanyNameFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form("value" -> text("companyName.error.required").verifying(validCompanyName()))

  private def validCompanyName(): Constraint[String] =
    Constraint {
      case companyName if companyName.length > CompanyName.maxLength               =>
        Invalid(ValidationError("companyName.error.length"))
      case companyName if !companyName.matches(CompanyName.companyNameRegex.regex) =>
        Invalid(ValidationError("companyName.error.invalid"))
      case companyName if companyName.trim.isEmpty                                 => Invalid(ValidationError("companyName.error.required"))
      case _                                                                       => Valid
    }

}
