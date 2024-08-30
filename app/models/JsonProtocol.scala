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

import enumeratum.EnumFormats
import models.DataModel._
import play.api.libs.json._
import shapeless.tag.@@

trait SimpleJson {

  def validatedStringFormat(A: ValidatedType[String], name: String) = new Format[String @@ A.Tag] {
    override def reads(json: JsValue): JsResult[String @@ A.Tag] = json match {
      case JsString(value) =>
        A.validateAndTransform(value) match {
          case Some(v) => JsSuccess(A(v))
          case None    => JsError(s"Expected a valid $name, got $value instead")
        }
      case xs: JsValue     => JsError(JsPath -> JsonValidationError(Seq(s"""Expected a valid $name, got $xs instead""")))
    }

    override def writes(o: String @@ A.Tag): JsValue = JsString(o)
  }

  implicit val nonEmptyStringFormat: Format[NonEmptyString] = new Format[NonEmptyString] {
    override def reads(json: JsValue): JsResult[NonEmptyString] = json match {
      case JsString(value) if value.nonEmpty => JsSuccess(NonEmptyString.apply(value))
      case _                                 => JsError((JsPath \ "value") -> JsonValidationError(Seq(s"Expected non empty string, got $json")))
    }

    override def writes(o: NonEmptyString): JsValue = JsString(o)
  }

  implicit val postcodeFormat                  = validatedStringFormat(Postcode, "postcode")
  implicit val phoneNumberFormat               = validatedStringFormat(PhoneNumber, "phone number")
  implicit val utrFormat                       = validatedStringFormat(UTR, "UTR")
  implicit val safeIfFormat                    = validatedStringFormat(SafeId, "SafeId")
  implicit val countryCodeFormat               = validatedStringFormat(CountryCode, "country code")
  implicit val buildingSocietyRollNumberFormat =
    validatedStringFormat(BuildingSocietyRollNumber, "building society roll number")
  implicit val restrictiveFormat               = validatedStringFormat(RestrictiveString, "name")
  implicit val companyNameFormat               = validatedStringFormat(CompanyName, "company name")
  implicit val mandatoryAddressLineFormat      = validatedStringFormat(AddressLine, "address line")
  implicit val dstRegNoFormat                  = validatedStringFormat(DSTRegNumber, "Digital Services Tax Registration Number")

}

object BackendAndFrontendJson extends SimpleJson {

  implicit val foreignAddressFormat: OFormat[ForeignAddress] = Json.format[ForeignAddress]
  implicit val ukAddressFormat: OFormat[UkAddress] = Json.format[UkAddress]
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
  implicit val companyFormat: OFormat[Company] = Json.format[Company]
  implicit val companyRegWrapperFormat: OFormat[CompanyRegWrapper] = Json.format[CompanyRegWrapper]

}