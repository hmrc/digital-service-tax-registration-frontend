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

import forms.mappings.Constraints.Address

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.CompanyRegisteredOfficeUkAddress

class CompanyRegisteredOfficeUkAddressFormProvider @Inject() extends Mappings {
  private val maximumLength = 35
  private val buildingOrStreetRequired = "companyRegisteredOfficeUkAddress.error.buildingorstreet.required"

  def apply(): Form[CompanyRegisteredOfficeUkAddress] = Form(
    mapping(
      "buildingorstreet" -> text(buildingOrStreetRequired)
        .verifying(
          regexp(Address.addressRegex, "companyRegisteredOfficeUkAddress.error.buildingOrStreet.invalidChar"),
          maxLength(maximumLength, "companyRegisteredOfficeUkAddress.error.buildingorstreet.length")),
      "buildingorstreet2" -> optional(
        text()
          .verifying(regexp(Address.addressRegex, "companyRegisteredOfficeUkAddress.error.buildingOrStreet2.invalidChar"),
            maxLength(maximumLength, "companyRegisteredOfficeUkAddress.error.buildingorstreet2.length"))),
      "town" -> optional(
        text()
          .verifying(regexp(Address.addressRegex, "companyRegisteredOfficeUkAddress.error.town.invalidChar"),
            maxLength(maximumLength, "companyRegisteredOfficeUkAddress.error.town.length"))),
      "county" -> optional(
        text()
          .verifying(regexp(Address.addressRegex, "companyRegisteredOfficeUkAddress.error.county.invalidChar"),
            maxLength(maximumLength, "companyRegisteredOfficeUkAddress.error.county.length"))),
      "postcode" -> text("contactUkAddress.error.postcode.required")
        .verifying(postcode("company.registeredOffice.postcode.required"))
    )(CompanyRegisteredOfficeUkAddress.apply)(x => Some((x.buildingorstreet, x.buildingorstreet2, x.town, x.county, x.postcode)))

  )
}


