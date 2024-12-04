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

import forms.mappings.AddressMappings
import models.{Address, InternationalAddress, Location, UkAddress}
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

sealed trait AddressFormProvider extends AddressMappings {
  val errorKeyPrefix: String
  def createForm: Form[_ <: Address]
}

trait UkAddressFormProvider extends AddressFormProvider {

  override val errorKeyPrefix: String = "ukAddress"

  override def createForm: Form[UkAddress] = Form[UkAddress](
    mapping(
      "line1" -> addressLineMapping("line1"),
      "line2" -> optional(addressLineMapping("line2")),
      "line3" -> optional(addressLineMapping("line3")),
      "line4" -> optional(addressLineMapping("line4")),
      postcodeMapping
    )(UkAddress.apply)(UkAddress.unapply)
  )
}

trait InternationalAddressFormProvider extends AddressFormProvider {

  val location: Location

  override val errorKeyPrefix: String = "internationalAddress"

  override def createForm: Form[InternationalAddress] = Form[InternationalAddress](
    mapping(
      "line1" -> addressLineMapping("line1"),
      "line2" -> optional(addressLineMapping("line2")),
      "line3" -> optional(addressLineMapping("line3")),
      "line4" -> optional(addressLineMapping("line4")),
      countryMapping(location)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}