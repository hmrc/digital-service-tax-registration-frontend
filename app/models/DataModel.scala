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

import shapeless.tag.@@
import shapeless.{:: => _}

import java.time.LocalDate

object DataModel extends SimpleJson {

  type UTR = String @@ UTR.Tag
  object UTR
      extends RegexValidatedString(
        "^[0-9]{10}$",
        _.replaceAll(" ", "")
      )

  type SafeId = String @@ SafeId.Tag
  object SafeId
      extends RegexValidatedString(
        "^[A-Z0-9]{1,15}$"
      )

  type Postcode = String @@ Postcode.Tag
  object Postcode
      extends RegexValidatedString(
        """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$""",
        _.trim.replaceAll("[ \\t]+", " ").toUpperCase
      )

  type NonEmptyString = String @@ NonEmptyString.Tag
  object NonEmptyString extends ValidatedType[String] {
    def validateAndTransform(in: String): Option[String] =
      Some(in).filter(_.nonEmpty)
  }

  type CompanyName = String @@ CompanyName.Tag
  object CompanyName
      extends RegexValidatedString(
        regex = """^[a-zA-Z0-9 '&.-]{1,105}$"""
      )

  type AddressLine = String @@ AddressLine.Tag
  object AddressLine
      extends RegexValidatedString(
        regex = """^[a-zA-Z0-9 '&.-]{1,35}$"""
      )

  type RestrictiveString = String @@ RestrictiveString.Tag
  object RestrictiveString
      extends RegexValidatedString(
        """^[a-zA-Z'&-^]{1,35}$"""
      )

  type CountryCode = String @@ CountryCode.Tag
  object CountryCode
      extends RegexValidatedString(
        """^[A-Z][A-Z]$""",
        _.toUpperCase match {
          case "UK"  => "GB"
          case other => other
        }
      )

  type BuildingSocietyRollNumber = String @@ BuildingSocietyRollNumber.Tag
  object BuildingSocietyRollNumber
      extends RegexValidatedString(
        """^[A-Za-z0-9 -]{1,18}$"""
      )

  type PhoneNumber = String @@ PhoneNumber.Tag
  object PhoneNumber
      extends RegexValidatedString(
        "^[A-Z0-9 \\-]{1,30}$"
      )

  type DSTRegNumber = String @@ DSTRegNumber.Tag
  object DSTRegNumber
      extends RegexValidatedString(
        "^([A-Z]{2}DST[0-9]{10})$"
      )

  implicit val orderDate = new cats.Order[LocalDate] {
    def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }
}
