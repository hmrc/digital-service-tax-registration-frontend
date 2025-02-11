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

import base.SpecBase
import generators.ModelGenerators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.{CheckCompanyRegisteredOfficeAddressPage, CompanyNamePage, CompanyRegisteredOfficeUkAddressPage}
import queries.Settable

class CompanyRegWrapperSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelGenerators {

  def completeUKUserAnswers(companyName: String, ukAddress: UkAddress): UserAnswers =
    UserAnswers("id")
      .set(CompanyNamePage, companyName)
      .success
      .value
      .set(CheckCompanyRegisteredOfficeAddressPage, true)
      .success
      .value
      .set(CompanyRegisteredOfficeUkAddressPage, ukAddress)
      .success
      .value

  def completeInternationalUserAnswers(companyName: String, internationalAddress: InternationalAddress): UserAnswers =
    UserAnswers("id")
      .set(CompanyNamePage, companyName)
      .success
      .value
      .set(CheckCompanyRegisteredOfficeAddressPage, false)
      .success
      .value
  // TODO Add International Company Address here when it is implemented

  "CompanyRegWrapper" - {

    "when .getFromUserAnswers is called" - {

      "must bundle a UK company from user answers and use safe ID flag" in {

        forAll(genCompany.suchThat(_.address.isInstanceOf[UkAddress]), Gen.oneOf(true, false)) { (company, useSafeId) =>
          CompanyRegWrapper
            .getFromUserAnswers(
              completeUKUserAnswers(company.name, company.address.asInstanceOf[UkAddress]),
              useSafeId
            )
            .value mustBe CompanyRegWrapper(company, useSafeId = useSafeId)
        }
      }

      "must bundle a International company from user answers and use safe ID flag" in pending

      "must return None" - {

        "for UK company when" - {

          val specParams = Seq[(String, Settable[_])](
            ("'company name'", CompanyNamePage),
            ("'is address in the UK'", CheckCompanyRegisteredOfficeAddressPage),
            ("'UK address'", CompanyRegisteredOfficeUkAddressPage)
          )

          specParams foreach { x =>
            s"${x._1} is missing from user answers" in {
              forAll(genCompanyName, arbitraryUkAddress.arbitrary) { (name, address) =>
                val userAnswers = completeUKUserAnswers(name, address).remove(x._2).success.value
                assert(CompanyRegWrapper.getFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }

        "for International company when" - {

          val specParams = Seq[(String, Settable[_])](
            ("'company name'", CompanyNamePage),
            ("'is address in the UK'", CheckCompanyRegisteredOfficeAddressPage),
            (
              "'International address'",
              CompanyRegisteredOfficeUkAddressPage
            ) // TODO change to International address page when implemented
          )

          specParams foreach { x =>
            s"${x._1} is missing from user answers" in {
              forAll(genCompanyName, arbitraryInternationalAddress.arbitrary) { (name, address) =>
                val userAnswers = completeInternationalUserAnswers(name, address).remove(x._2).success.value
                assert(CompanyRegWrapper.getFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }
      }
    }
  }
}
