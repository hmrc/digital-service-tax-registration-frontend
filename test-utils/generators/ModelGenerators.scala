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

package generators

import forms.mappings.Constraints
import models.ContactUkAddress
import models.CompanyRegisteredOfficeUkAddress
import org.scalacheck.Arbitrary
import wolfendale.scalacheck.regexp.RegexpGen
import org.scalacheck.Arbitrary.arbitrary

trait ModelGenerators {

  implicit lazy val arbitraryContactUkAddress: Arbitrary[ContactUkAddress] =
    Arbitrary {
      for {
        buildingOrStreet <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield ContactUkAddress(buildingOrStreet, None, None, None, postcode)
    }
  val genPostcode = RegexpGen.from(Constraints.postcodeRegex.regex)
  val genCompanyName = RegexpGen.from(Constraints.CompanyName.companyNameRegex.regex)

  implicit lazy val arbitraryCompanyRegisteredOfficeUkAddress: Arbitrary[CompanyRegisteredOfficeUkAddress] =
    Arbitrary {
      for {
        buildingorstreet <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield CompanyRegisteredOfficeUkAddress(buildingorstreet, None, None, None, postcode)
    }
}
