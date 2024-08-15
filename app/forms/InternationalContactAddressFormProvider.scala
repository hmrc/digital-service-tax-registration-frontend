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

import forms.mappings.{Mappings, StopOnFirstFail}
import models.{Country, InternationalContactAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class InternationalContactAddressFormProvider @Inject() extends Mappings {

  val maxLength = 35
  val regex = """^[A-Za-z0-9 \-,.&']*$"""

  def apply(countryList: Seq[Country]): Form[InternationalContactAddress] = Form(
    mapping(
      "line1" -> text("internationalContactAddress.error.line1.required")
        .verifying(
          StopOnFirstFail[String](
            regexp(regex, "internationalContactAddress.error.line1.invalid"),
            maxLength(maxLength, "internationalContactAddress.error.line1.length")
          )),
      "line2" -> optional(text("internationalContactAddress.error.line2.required")
        .verifying(regexp(regex, "internationalContactAddress.error.line2.invalid"))
        .verifying(maxLength(maxLength, "internationalContactAddress.error.line2.length"))),
      "line3" -> optional(text("internationalContactAddress.error.line3.required")
        .verifying(regexp(regex, "internationalContactAddress.error.line3.invalid"))
        .verifying(maxLength(maxLength, "internationalContactAddress.error.line3.length"))),
      "line4" -> optional(text("internationalContactAddress.error.line4.required")
        .verifying(regexp(regex, "internationalContactAddress.error.line4.invalid"))
        .verifying(maxLength(maxLength, "internationalContactAddress.error.line4.length"))),
      "country" -> text("internationalContactAddress.error.countryCode.required")
        .verifying("internationalContactAddress.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(InternationalContactAddress.apply)(x => Some((x.line1, x.line2, x.line3, x.line4, x.country)))
  )
}
