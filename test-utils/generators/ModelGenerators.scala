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
import models.DataValues.DST_EPOCH
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaChar, alphaNumStr, alphaStr, numStr}
import org.scalacheck.{Arbitrary, Gen}
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.{Instant, LocalDate, ZoneOffset}

trait ModelGenerators {

  implicit lazy val arbitraryContactPersonName: Arbitrary[ContactPersonName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield ContactPersonName(firstName, lastName)
    }

  val genCompanyName = RegexpGen.from(Constraints.CompanyName.companyNameRegex.regex).suchThat(_.nonEmpty)

  implicit lazy val arbitraryLocation: Arbitrary[Country] =
    Arbitrary {
      for {
        name  <- Arbitrary.arbitrary[String]
        code  <- Gen.pick(2, 'A' to 'Z')
        type1 <- Gen.oneOf(Seq("country"))
      } yield Country(name, code.mkString, type1)
    }

  private val nonEmptyStringGen = alphaStr.suchThat(_.nonEmpty)

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1    <- nonEmptyStringGen
        line2    <- Gen.option(nonEmptyStringGen)
        line3    <- Gen.option(nonEmptyStringGen)
        line4    <- Gen.option(nonEmptyStringGen)
        postcode <- genPostcode
      } yield UkAddress(line1, line2, line3, line4, postcode)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1       <- nonEmptyStringGen
        line2       <- Gen.option(nonEmptyStringGen)
        line3       <- Gen.option(nonEmptyStringGen)
        line4       <- Gen.option(nonEmptyStringGen)
        countryCode <- genCountryCode
      } yield InternationalAddress(line1, line2, line3, line4, countryCode)
    }

  val genPostcode: Gen[String] = RegexpGen.from(Constraints.postcodeRegex)

  val genCountryCode: Gen[String] = for {
    char1 <- alphaChar
    char2 <- alphaChar
  } yield s"$char1$char2"

  val genCompany: Gen[Company] =
    for {
      name    <- genCompanyName
      address <- Gen.oneOf(arbitraryUkAddress.arbitrary, arbitraryInternationalAddress.arbitrary)
    } yield Company(name, address)

  val genCompanyRegWrapper: Gen[CompanyRegWrapper] =
    for {
      company   <- genCompany
      utr       <- Gen.option(numStr.suchThat(_.nonEmpty))
      safeId    <- Gen.option(alphaNumStr.suchThat(_.nonEmpty))
      useSafeId <- Gen.oneOf(true, false)
    } yield CompanyRegWrapper(
      company,
      utr,
      safeId,
      useSafeId
    )

  val genEmail: Gen[String] =
    for {
      username  <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      domain    <- nonEmptyStringGen
      topDomain <- Gen.oneOf("com", "gov.uk", "co.uk", "net", "org", "io")
    } yield s"$username@$domain.$topDomain"

  val genContactDetails: Gen[ContactDetails] =
    for {
      name    <- arbitraryContactPersonName.arbitrary
      phoneNo <- Gen.numStr.suchThat(_.nonEmpty)
      email   <- genEmail
    } yield ContactDetails(name.firstName, name.lastName, phoneNo, email)

  val genRegistration: Gen[Registration] = {

    val datesBetween: Gen[LocalDate] = {

      def toMillis(date: LocalDate): Long = date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

      Gen.choose(toMillis(DST_EPOCH), toMillis(LocalDate.now(ZoneOffset.UTC))).map { millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
      }
    }

    for {
      companyReg          <- genCompanyRegWrapper
      alternativeContact  <- Gen.option(Gen.oneOf(arbitraryUkAddress.arbitrary, arbitraryInternationalAddress.arbitrary))
      ultimateParent      <- Gen.option(genCompany)
      contact             <- genContactDetails
      dateLiable          <- datesBetween
      accountingPeriodEnd <- Gen.choose(DST_EPOCH.plusDays(1), dateLiable.plusYears(1)).suchThat(_ => true)
    } yield Registration(
      companyReg,
      alternativeContact,
      ultimateParent,
      contact,
      dateLiable,
      accountingPeriodEnd,
      None
    )
  }
}
