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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

class CorporationTaxEnterUtrFormProvider @Inject() extends Mappings {

  val uniqueTaxReferenceMaxRegex = "^[0-9]{10}$"
  val errorRequired              = "corporationTaxEnterUtr.error.required"
  val errorInvalid               = "corporationTaxEnterUtr.error.invalid"

  private def validUTR: Constraint[String] =
    Constraint("constraints.utr") { utr =>
      utr.filterNot(_.isWhitespace) match {
        case s if s.trim.isEmpty                         => Invalid(ValidationError(errorRequired))
        case s if !s.matches(uniqueTaxReferenceMaxRegex) =>
          Invalid(ValidationError(errorInvalid, uniqueTaxReferenceMaxRegex))
        case _                                           => Valid
      }
    }

  def apply(): Form[String] =
    Form(
      "value" -> text(errorRequired).verifying(validUTR)
    )
}
