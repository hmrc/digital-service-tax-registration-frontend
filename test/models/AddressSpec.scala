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

package models

import base.SpecBase
import generators.ModelGenerators
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class AddressSpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {

  val name        = "Big Corp"
  val line1       = "123 Test Street"
  val line2       = "Business Park"
  val line3       = "Long Road"
  val line4       = "London"
  val postcode    = "TE5 5ST"
  val countryCode = "US"
  val safeId      = "13979F8H2EHRWBGSDI9"

  "Address" - {

    "must serialise UK Address JSON as UK Address" in {

      val addressJsonStr =
        s"""
          |{
          |  "_type":"uk.gov.hmrc.digitalservicestax.data.UkAddress",
          |  "line1":"$line1",
          |  "line2":"$line2",
          |  "line3":"$line3",
          |  "line4":"$line4",
          |  "postalCode":"$postcode"
          |}
          |""".stripMargin

      Json.parse(addressJsonStr).as[Address] mustBe UkAddress(line1, Some(line2), Some(line3), Some(line4), postcode)
    }

    "must serialise Foreign Address JSON as International Address" in {

      val addressJsonStr =
        s"""
           |{
           |  "_type":"uk.gov.hmrc.digitalservicestax.data.ForeignAddress",
           |  "line1":"$line1",
           |  "line2":"$line2",
           |  "line3":"$line3",
           |  "line4":"$line4",
           |  "countryCode":"$countryCode"
           |}
           |""".stripMargin

      Json.parse(addressJsonStr).as[Address] mustBe InternationalAddress(
        line1,
        Some(line2),
        Some(line3),
        Some(line4),
        countryCode
      )
    }

    "must serialise UK address with country code and postcode JSON as UK Address" in {

      val addressJsonStr =
        s"""
           |{
           |  "_type":"uk.gov.hmrc.digitalservicestax.data.UkAddress",
           |  "line1":"$line1",
           |  "line2":"$line2",
           |  "line3":"$line3",
           |  "line4":"$line4",
           |  "postalCode":"$postcode",
           |  "countryCode":"GB"
           |}
           |""".stripMargin

      Json.parse(addressJsonStr).as[Address] mustBe UkAddress(line1, Some(line2), Some(line3), Some(line4), postcode)
    }

    "must throw when parsing Address JSON" - {

      "when both postcode is set but address is international" in {

        val addressJsonStr =
          s"""
             |{
             |  "_type":"uk.gov.hmrc.digitalservicestax.data.UkAddress",
             |  "line1":"$line1",
             |  "line2":"$line2",
             |  "line3":"$line3",
             |  "line4":"$line4",
             |  "postalCode":"$postcode",
             |  "countryCode":"$countryCode"
             |}
             |""".stripMargin

        an[IllegalArgumentException] mustBe thrownBy {
          Json.parse(addressJsonStr).as[Address]
        }
      }

      "when both postcode and country are missing" in {

        val addressJsonStr =
          s"""
             |{
             |  "_type":"uk.gov.hmrc.digitalservicestax.data.UkAddress",
             |  "line1":"$line1",
             |  "line2":"$line2",
             |  "line3":"$line3",
             |  "line4":"$line4"
             |}
             |""".stripMargin

        an[IllegalArgumentException] mustBe thrownBy {
          Json.parse(addressJsonStr).as[Address]
        }
      }
    }
  }

  "UK Address" - {

    "when .countryCode is called" - {

      "must return the correct value" in {
        forAll(arbitraryUkAddress.arbitrary) { address =>
          address.countryCode mustEqual "GB"
        }
      }
    }

    "when .asAddressLines is called" - {

      "must contain mandatory line 1 and postcode along with any set optional values" in {

        forAll(arbitraryUkAddress.arbitrary) { address =>
          val lines = address.asAddressLines

          lines.head mustBe address.line1
          if (address.line2.isDefined) lines must contain(address.line2.get)
          if (address.line3.isDefined) lines must contain(address.line3.get)
          if (address.line4.isDefined) lines must contain(address.line4.get)
          lines.last mustBe address.postalCode

          lines.size mustBe (Seq(address.line1, address.postalCode) ++ Seq(
            address.line2,
            address.line3,
            address.line4
          ).flatten).size
        }
      }
    }
  }

  "International Address" - {

    "when .postalCode is called" - {

      "must return an empty String" - {

        forAll(arbitraryInternationalAddress.arbitrary) { address =>
          assert(address.postalCode == "")
        }
      }
    }

    "when .toAddressLines is called" - {

      "must contain mandatory line 1 and country name along with any set optional values" in {

        val mockLocation = mock[Location]
        val name         = "Republic of Testing"

        forAll(arbitraryInternationalAddress.arbitrary) { address =>
          when(mockLocation.name(ArgumentMatchers.eq(address.countryCode))).thenReturn(name)

          val lines = address.asAddressLines(mockLocation)

          lines.head mustBe address.line1
          if (address.line2.isDefined) lines must contain(address.line2.get)
          if (address.line3.isDefined) lines must contain(address.line3.get)
          if (address.line4.isDefined) lines must contain(address.line4.get)
          lines.last mustBe name

          lines.size mustBe (Seq(address.line1, name) ++ Seq(address.line2, address.line3, address.line4).flatten).size
        }
      }
    }
  }
}
