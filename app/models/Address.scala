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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json._

sealed trait Address {
  def line1: String
  def line2: Option[String]
  def line3: Option[String]
  def line4: Option[String]
  def countryCode: String
  def postalCode: String
}

object Address {
  implicit val reads: Reads[Address] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").readNullable[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "postalCode").readNullable[String] and
      (__ \ "countryCode").readNullable[String]
  )(applyJson _)

  private def applyJson(
    line1: String,
    line2: Option[String],
    line3: Option[String],
    line4: Option[String],
    postalCode: Option[String],
    countryCode: Option[String]
  ): Address =
    (postalCode, countryCode) match {
      case (Some(postcode), None)       => UkAddress(line1, line2, line3, line4, postcode)
      case (None, Some(countryCode))    => InternationalAddress(line1, line2, line3, line4, countryCode)
      case (Some(postcode), Some("GB")) => UkAddress(line1, line2, line3, line4, postcode)
      case _                            => throw new IllegalArgumentException("Could not instantiate Address from Json")
    }

  // This is necessary to match the `_type` discriminator in the backend Registration reads
  implicit val cfg: Aux[Json.MacroOptions] = JsonConfiguration(
    typeNaming = JsonNaming {
      case name if name.contains("InternationalAddress") =>
        "uk.gov.hmrc.digitalservicestax.data.ForeignAddress"
      case ukName                                        =>
        s"uk.gov.hmrc.digitalservicestax.data.UkAddress"
    }
  )

  implicit val writes: OWrites[Address] = Json.writes[Address]
}

final case class UkAddress(
  line1: String,
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  postalCode: String
) extends Address {
  def countryCode = "GB"

  def asAddressLines: Seq[String] = Seq(
    Some(line1),
    line2,
    line3,
    line4,
    Some(postalCode)
  ).flatten
}

object UkAddress {
  implicit val reads: Reads[UkAddress]    = Json.reads[UkAddress]
  implicit val writes: OWrites[UkAddress] = Json.writes[UkAddress]
}

final case class InternationalAddress(
  line1: String,
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  countryCode: String
) extends Address {
  def postalCode: String = ""

  def asAddressLines(location: Location): Seq[String] = Seq(
    Some(line1),
    line2,
    line3,
    line4,
    Some(location.name(countryCode))
  ).flatten
}

object InternationalAddress {

  implicit val reads: Reads[InternationalAddress]    = Json.reads[InternationalAddress]
  implicit val writes: OWrites[InternationalAddress] = Json.writes[InternationalAddress]
}
