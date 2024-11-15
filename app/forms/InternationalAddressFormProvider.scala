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

import forms.mappings.Mappings
import models.{Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class InternationalAddressFormProvider @Inject() extends Mappings {

  val maxLength = 35
  val regex     = """^[A-Za-z0-9 \-,.&']*$"""

  def apply(countryList: Seq[Country]): Form[InternationalAddress] = Form(
    mapping(
      "line1"   -> text("internationalAddress.error.line1.required")
        .verifying(
          regexp(regex, "internationalAddress.error.line1.invalid"),
          maxLength(maxLength, "internationalAddress.error.line1.length")
        ),
      "line2"   -> optionalText(
        "internationalAddress.error.line2.invalid",
        "internationalAddress.error.line2.length",
        regex,
        maxLength
      ),
      "line3"   -> optionalText(
        "internationalAddress.error.line3.invalid",
        "internationalAddress.error.line3.length",
        regex,
        maxLength
      ),
      "line4"   -> optionalText(
        "internationalAddress.error.line4.invalid",
        "internationalAddress.error.line4.length",
        regex,
        maxLength
      ),
      "country" -> text("internationalAddress.error.countryCode.required")
        .verifying("internationalAddress.error.country.required", value => countryList.exists(_.code == value))
    )(InternationalAddress.apply)(x => Some((x.line1, x.line2, x.line3, x.line4, x.countryCode)))
  )
}
