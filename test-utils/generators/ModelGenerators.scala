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

import models.CompanyRegisteredOfficeUkAddress
import models.{ContactUkAddress, _}
import org.scalacheck.Arbitrary.arbitrary
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

  implicit lazy val arbitraryUltimateParentCompanyUkAddress: Arbitrary[UltimateParentCompanyUkAddress] =
    Arbitrary {
      for {
        buildingOrStreet <- arbitrary[String]
        postcode         <- arbitrary[String]
      } yield UltimateParentCompanyUkAddress(buildingOrStreet, None, None, None, postcode)
    }

  implicit lazy val arbitraryContactUkAddress: Arbitrary[ContactUkAddress] =
    Arbitrary {
      for {
        buildingOrStreet <- arbitrary[String]
        postcode         <- arbitrary[String]
      } yield ContactUkAddress(buildingOrStreet, None, None, None, postcode)
    }
  val genCompanyName                                                       = RegexpGen.from(Constraints.CompanyName.companyNameRegex.regex)

  implicit lazy val arbitraryCompanyRegisteredOfficeUkAddress: Arbitrary[CompanyRegisteredOfficeUkAddress] =
    Arbitrary {
      for {
        buildingorstreet <- arbitrary[String]
        postcode         <- arbitrary[String]
      } yield CompanyRegisteredOfficeUkAddress(buildingorstreet, None, None, None, postcode)
    }
  implicit lazy val arbitraryLocation: Arbitrary[Country]                                                  =
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
        line1 <- arbitrary[String]
        line2 <- arbitrary[Option[String]]
        line3 <- arbitrary[Option[String]]
        line4 <- arbitrary[Option[String]]
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
        countryCode <- Arbitrary.arbitrary[String]
      } yield InternationalAddress(line1, line2, line3, line4, countryCode)
    }
  val genPostcode: Gen[String] = RegexpGen.from(Constraints.postcodeRegex.regex)
}
