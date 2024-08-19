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

import forms.mappings.Constraints.Address.{addressRegex, maximumLength}

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.ContactUkAddress

class ContactUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[ContactUkAddress] = {
    Form(
      mapping(
        "building-or-street" -> text("contactUkAddress.error.buildingOrStreet.required")
          .verifying(
            regexp(addressRegex, "contactUkAddress.error.buildingOrStreet.invalid"),
            maxLength(maximumLength, "contactUkAddress.error.buildingOrStreet.length")),
        "building-or-street-line-2" -> optionalText("contactUkAddress.error.buildingOrStreetLine2.invalid",
          "contactUkAddress.error.buildingOrStreetLine2.length", addressRegex, maximumLength),
        "town-or-city" -> optionalText("contactUkAddress.error.townOrCity.invalid",
          "contactUkAddress.error.townOrCity.length", addressRegex, maximumLength),
        "county" -> optionalText("contactUkAddress.error.county.invalid",
          "contactUkAddress.error.county.length", addressRegex, maximumLength),
        "postcode" -> text("contactUkAddress.error.postcode.required")
          .verifying(postcode("company.registeredOffice.postcode.required"))
      )(ContactUkAddress.apply)(x => Some((x.buildingOrStreet, x.buildingOrStreetLine2, x.townOrCity, x.county, x.postcode)))
    )
  }
}
