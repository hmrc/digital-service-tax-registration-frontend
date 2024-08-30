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

import cats.implicits._
import forms.mappings.Constraints
import models.DataModel._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.cats.implicits.genInstances
import org.scalacheck.{Arbitrary, Gen}
import shapeless.tag.@@
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators {

  implicit def arbAddressLine: Arbitrary[AddressLine] = Arbitrary(AddressLine.gen)
  implicit def arbPostcode: Arbitrary[Postcode]       = Arbitrary(Postcode.gen.retryUntil(x => x == x.replaceAll(" ", "")))
  implicit def arbCountryCode: Arbitrary[CountryCode] = Arbitrary(CountryCode.gen)
  implicit def arbUtr: Arbitrary[UTR] = Arbitrary(UTR.gen)
  implicit def arbCompanyName: Arbitrary[CompanyName] = Arbitrary(CompanyName.gen)

  implicit class RichRegexValidatedString[A <: RegexValidatedString](val in: A) {
    def gen: Gen[String @@ RichRegexValidatedString.this.in.Tag] = RegexpGen.from(in.regex).map(in.apply)
  }

  implicit lazy val arbitraryUltimateParentCompanyUkAddress: Arbitrary[UltimateParentCompanyUkAddress] =
    Arbitrary {
      for {
        buildingOrStreet <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield UltimateParentCompanyUkAddress(buildingOrStreet, None, None, None, postcode)
    }

  implicit lazy val arbitraryContactUkAddress: Arbitrary[ContactUkAddress] =
    Arbitrary {
      for {
        buildingOrStreet <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield ContactUkAddress(buildingOrStreet, None, None, None, postcode)
    }
  val genCompanyName = RegexpGen.from(Constraints.CompanyName.companyNameRegex.regex)

  implicit lazy val arbitraryCompanyRegisteredOfficeUkAddress: Arbitrary[CompanyRegisteredOfficeUkAddress] =
    Arbitrary {
      for {
        buildingorstreet <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield CompanyRegisteredOfficeUkAddress(buildingorstreet, None, None, None, postcode)
    }
  implicit lazy val arbitraryLocation: Arbitrary[Country] =
    Arbitrary {
      for {
        name <- Arbitrary.arbitrary[String]
        code <- Gen.pick(2, 'A' to 'Z')
        type1 <- Gen.oneOf(Seq("country"))
      } yield Country(name, code.mkString, type1)
    }

  implicit lazy val arbitraryInternationalContactAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1 <- Arbitrary.arbitrary[String]
        line2 <- Arbitrary.arbitrary[Option[String]]
        line3 <- Arbitrary.arbitrary[Option[String]]
        line4 <- Arbitrary.arbitrary[Option[String]]
        countryCode <- Arbitrary.arbitrary[Country]
      } yield InternationalAddress(line1, line2, line3, line4, countryCode)
    }
  val genPostcode: Gen[String] = RegexpGen.from(Constraints.postcodeRegex.regex)

  implicit def arbAddr: Arbitrary[Address] = Arbitrary {

    val ukGen: Gen[Address] =
      (
        arbitrary[AddressLine],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Postcode]
      ).mapN(UkAddress.apply)

    val foreignGen: Gen[Address] =
      (
        arbitrary[AddressLine],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[CountryCode]
      ).mapN(ForeignAddress.apply)

    Gen.oneOf(ukGen, foreignGen)

  }

  implicit def arbCo: Arbitrary[Company] = Arbitrary(
    (
      arbitrary[CompanyName],
      arbitrary[Address]
    ).mapN(Company.apply)
  )

  implicit def arbCoRegWrap: Arbitrary[CompanyRegWrapper] = Arbitrary(
    (
      arbitrary[Company],
      Gen.const(none[UTR]),
      Gen.const(none[SafeId]),
      Gen.const(false)
    ).mapN(CompanyRegWrapper.apply)
  )
}
