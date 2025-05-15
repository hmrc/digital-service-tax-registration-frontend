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

import play.api.libs.json.{Json, OFormat}

final case class CompanyRegWrapper(
  company: Company,
  utr: Option[String] = None,
  safeId: Option[String] = None,
  useSafeId: Boolean = false
)

object CompanyRegWrapper {

  def getFromUserAnswers(ua: UserAnswers, useSafeId: Boolean = false): Option[CompanyRegWrapper] =
    for {
      company <- Company.getFromUserAnswers(ua)
    } yield CompanyRegWrapper(company, useSafeId = useSafeId)

  implicit val format: OFormat[CompanyRegWrapper] = Json.format[CompanyRegWrapper]
}
