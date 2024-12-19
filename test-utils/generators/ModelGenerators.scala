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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaChar
import org.scalacheck.{Arbitrary, Gen}
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators {

  implicit lazy val arbitraryContactPersonName: Arbitrary[ContactPersonName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield ContactPersonName(firstName, lastName)
    }

  val genCompanyName = RegexpGen.from(Constraints.CompanyName.companyNameRegex.regex)

  implicit lazy val arbitraryLocation: Arbitrary[Country] =
    Arbitrary {
      for {
        name  <- Arbitrary.arbitrary[String]
        code  <- Gen.pick(2, 'A' to 'Z')
        type1 <- Gen.oneOf(Seq("country"))
      } yield Country(name, code.mkString, type1)
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1    <- arbitrary[String]
        line2    <- arbitrary[Option[String]]
        line3    <- arbitrary[Option[String]]
        line4    <- arbitrary[Option[String]]
        postcode <- genPostcode
      } yield UkAddress(line1, line2, line3, line4, postcode)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1       <- Arbitrary.arbitrary[String]
        line2       <- Arbitrary.arbitrary[Option[String]]
        line3       <- Arbitrary.arbitrary[Option[String]]
        line4       <- Arbitrary.arbitrary[Option[String]]
        countryCode <- genCountryCode
      } yield InternationalAddress(line1, line2, line3, line4, countryCode)
    }

  val genPostcode: Gen[String] = RegexpGen.from(Constraints.postcodeRegex)

  val genCountryCode: Gen[String] = for {
    char1 <- alphaChar
    char2 <- alphaChar
  } yield s"$char1$char2"
}
