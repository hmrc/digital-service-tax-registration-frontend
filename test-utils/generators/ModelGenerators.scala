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
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import models._
import org.scalacheck.{Arbitrary, Gen}
import wolfendale.scalacheck.regexp.RegexpGen

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

  implicit lazy val arbitraryLocation: Arbitrary[Country] =
    Arbitrary {
      for {
        name <- Arbitrary.arbitrary[String]
        code <- Gen.pick(2, 'A' to 'Z')
        type1 <- Gen.oneOf(Seq("country"))
      } yield Country(name, code.mkString, type1)
    }

  implicit lazy val arbitraryInternationalContactAddress: Arbitrary[InternationalContactAddress] =
    Arbitrary {
      for {
        line1 <- Arbitrary.arbitrary[String]
        line2 <- Arbitrary.arbitrary[Option[String]]
        line3 <- Arbitrary.arbitrary[Option[String]]
        line4 <- Arbitrary.arbitrary[Option[String]]
        countryCode <- Arbitrary.arbitrary[Country]
      } yield InternationalContactAddress(line1, line2, line3, line4, countryCode)
    }
  val genPostcode: Gen[String] = RegexpGen.from(Constraints.postcodeRegex.regex)
}
