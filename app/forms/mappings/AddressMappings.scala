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

package forms.mappings

import forms.mappings.Constraints.{Address, postcodeRegex}
import models.Location
import play.api.data.Mapping
import play.api.data.validation.{Constraint, Invalid, Valid}

trait AddressMappings extends Mappings {

  val errorKeyPrefix: String

  val length: Int = 35

  private def baseErrorString(line: String) = s"$errorKeyPrefix.error.$line"

  private def normalizePostcode(postcode: String): String =
    if (postcode.matches(postcodeRegex)) postcode else postcode.replaceAll("\\s+", "")

  protected def addressLineMapping(line: String): Mapping[String] =
    text(s"${baseErrorString(line)}.required")
      .verifying(
        firstError(
          maxLength(length, s"${baseErrorString(line)}.length"),
          regexp(Address.addressRegex, s"${baseErrorString(line)}.invalid")
        )
      )

  protected def postcodeMapping: (String, Mapping[String]) =
    "postcode" -> text(s"${baseErrorString("postcode")}.required")
      .transform[String](normalizePostcode, identity)
      .verifying(regexp(postcodeRegex, s"${baseErrorString("postcode")}.invalid"))

  protected def countryMapping(location: Location): (String, Mapping[String]) =
    "country" -> text(s"${baseErrorString("country")}.required").verifying(countryConstraintExcludeUK(location))

  private def countryConstraintExcludeUK(location: Location): Constraint[String] =
    Constraint {
      case str if str == "GB"                                             => Invalid(s"${baseErrorString("country")}.required")
      case str if location.countryListWithoutGB.map(_.code).contains(str) => Valid
      case _                                                              => Invalid(s"${baseErrorString("country")}.required")
    }
}
