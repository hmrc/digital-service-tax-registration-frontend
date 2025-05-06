/*
 * Copyright 2025 HM Revenue & Customs
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

import pages._
import play.api.libs.json.{Json, OFormat}

case class Company(
  name: String,
  address: Address
)

object Company { // TODO try to combine the below methods and have a flag to indicate parent company?

  def getFromUserAnswers(ua: UserAnswers): Option[Company] = {

    def addressOpt(isUk: Boolean): Option[Address] = if (isUk) {
      ua.get(CompanyRegisteredOfficeUkAddressPage)
    } else {
      None // TODO implement International Address answer here
    }

    for {
      name    <- ua.get(CompanyNamePage)
      isUk    <- ua.get(CheckCompanyRegisteredOfficeAddressPage)
      address <- addressOpt(isUk)
    } yield Company(name, address)
  }

  def getParentCompanyFromUserAnswers(ua: UserAnswers): Option[Company] = {

    def addressOpt(isUk: Boolean): Option[Address] = if (isUk) {
      ua.get(UltimateParentCompanyUkAddressPage)
    } else {
      ua.get(UltimateParentCompanyInternationalAddressPage)
    }

    for {
      name    <- ua.get(UltimateParentCompanyNamePage)
      isUk    <- ua.get(CheckUltimateGlobalParentCompanyInUkPage)
      address <- addressOpt(isUk)
    } yield Company(name, address)
  }

  implicit val format: OFormat[Company] = Json.format[Company]
}
