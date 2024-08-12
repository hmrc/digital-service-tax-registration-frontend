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
import forms.mappings.{Constraints, Mappings}
import play.api.data.Form
import play.api.data.Forms._
import models.ContactUkAddress

class ContactUkAddressFormProvider @Inject() extends Mappings {

  private val maximumLength = 35
  private val postcodeMaximumLength = 8
  private val buildingOrStreetRequired = "contactUkAddress.error.BuildingOrStreet.required"
  private val postcodeRequired = "contactUkAddress.error.Postcode.required"

  def apply(): Form[ContactUkAddress] = {
    Form(
      mapping(
        "Building or street" -> text(buildingOrStreetRequired)
          .verifying(
            firstError(
              isNotEmpty("Building or street", buildingOrStreetRequired),
              maxLength(maximumLength, "contactUkAddress.error.BuildingOrStreet.length"))),
        "Building or street line 2" -> optional(
          text()
            .verifying(maxLength(maximumLength, "contactUkAddress.error.BuildingOrStreet.length"))),
        "Town or city" -> optional(
          text()
            .verifying(maxLength(maximumLength, "contactUkAddress.error.BuildingOrStreet.length"))),
        "County" -> optional(
          text()
            .verifying(maxLength(maximumLength, "contactUkAddress.error.BuildingOrStreet.length"))),
        "Postcode" -> text(postcodeRequired)
          .verifying(
            firstError(
              isNotEmpty("Postcode", postcodeRequired),
              maxLength(postcodeMaximumLength, "contactUkAddress.error.Postcode.length"),
              regexp(Constraints.postcodeRegex.toString(), "error.invalid.postcode")))
      )(ContactUkAddress.apply)(x => Some((x.buildingOrStreet, x.buildingOrStreetLine2, x.townOrCity, x.county, x.postcode)))
    )
  }
}
