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
import services.DigitalServicesTaxService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

final case class Registration(
                               companyReg: CompanyRegWrapper,
                               alternativeContact: Option[Address],
                               ultimateParent: Option[Company],
                               contact: ContactDetails,
                               dateLiable: LocalDate,
                               accountingPeriodEnd: LocalDate,
                               registrationNumber: Option[String] = None
                             )

object Registration {

  def fromUserAnswers(ua: UserAnswers, service: DigitalServicesTaxService)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Registration]] = {

    def retrieveCompanyRegWrapper: Future[Option[CompanyRegWrapper]] =
      service.lookupCompany flatMap { companyRegWrapperOpt =>
        (companyRegWrapperOpt, ua.get(CheckCompanyRegisteredOfficePostcodePage), ua.get(CorporationTaxEnterUtrPage)) match {
          case (wrapper @ Some(_), _, _) =>
            Future.successful(wrapper)
          case (None, Some(postcode), Some(utr)) =>
            service.lookupCompany(utr, postcode) map {
              case None => CompanyRegWrapper.getFromUserAnswers(ua, useSafeId = true)
              case wrapper => wrapper.map(_.copy(utr = Some(utr)))
            }
          case _ =>
            Future.successful(CompanyRegWrapper.getFromUserAnswers(ua, useSafeId = true))
        }
      }

    def contactAddress: Option[Address] =
      (ua.get(ContactUkAddressPage), ua.get(InternationalContactAddressPage)) match {
        case (ukAddress @ Some(_), None) => ukAddress
        case (None, internationalAddress @ Some(_)) => internationalAddress
        case _ => None
      }

    retrieveCompanyRegWrapper map { companyReg =>
      for {
        wrapper             <- companyReg
        altContact           = contactAddress
        ultimateParentOpt    = Company.getParentCompanyFromUserAnswers(ua)
        contactDetails      <- ContactDetails.getFromUserAnswers(ua)
        dateLiable          <- ua.get(LiabilityStartDatePage)
        accountingPeriodEnd <- ua.get(AccountingPeriodEndDatePage)
      } yield Registration(
        wrapper,
        altContact,
        ultimateParentOpt,
        contactDetails,
        dateLiable,
        accountingPeriodEnd,
        None
      )
    }
  }

  implicit val format: OFormat[Registration] = Json.format[Registration]
}
