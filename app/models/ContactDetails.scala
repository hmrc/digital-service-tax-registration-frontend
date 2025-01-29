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

import pages.{ContactPersonEmailAddressPage, ContactPersonNamePage, ContactPersonPhoneNumberPage}
import play.api.libs.json.{Json, OFormat}

final case class ContactDetails(
                                 forename: String,
                                 surname: String,
                                 phoneNumber: String,
                                 email: String
                               )

object ContactDetails {

  def getFromUserAnswers(ua: UserAnswers): Option[ContactDetails] =
    for {
      name        <- ua.get(ContactPersonNamePage)
      phoneNumber <- ua.get(ContactPersonPhoneNumberPage)
      email       <- ua.get(ContactPersonEmailAddressPage)
    } yield ContactDetails(name.firstName, name.lastName, phoneNumber, email)

  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}
