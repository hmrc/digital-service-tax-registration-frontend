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
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages._
import queries.Settable

class CompanySpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelGenerators {

  def completeUKUserAnswers(companyName: String, ukAddress: UkAddress, isParentCompany: Boolean = false): UserAnswers = {

    val nameKey = if(isParentCompany) UltimateParentCompanyNamePage else CompanyNamePage
    val isUkKey = if(isParentCompany) CheckUltimateGlobalParentCompanyInUkPage else CheckCompanyRegisteredOfficeAddressPage
    val addressKey = if(isParentCompany) UltimateParentCompanyUkAddressPage else CompanyRegisteredOfficeUkAddressPage

    UserAnswers("id")
      .set(nameKey, companyName).success.value
      .set(isUkKey, true).success.value
      .set(addressKey, ukAddress).success.value
  }

  def completeInternationalUserAnswers(companyName: String, internationalAddress: InternationalAddress, isParentCompany: Boolean = false): UserAnswers = {

    val nameKey = if(isParentCompany) UltimateParentCompanyNamePage else CompanyNamePage
    val isUkKey = if(isParentCompany) CheckUltimateGlobalParentCompanyInUkPage else CheckCompanyRegisteredOfficeAddressPage
    val addressKey = if(isParentCompany) UltimateParentCompanyInternationalAddressPage else UltimateParentCompanyInternationalAddressPage //TODO change else option to International address page when implemented

    UserAnswers("id")
      .set(nameKey, companyName).success.value
      .set(isUkKey, false).success.value
      .set(addressKey, internationalAddress).success.value
  }


  "Company" - {

    "when .getFromUserAnswers is called" - {

      "must return a company" - {

        "when a UK Company is set in user answers" in {
          forAll(genCompanyName, arbitraryUkAddress.arbitrary) { (name, address) =>
            Company.getFromUserAnswers(completeUKUserAnswers(name, address)).value mustBe Company(name, address)
          }
        }

        "when an International Company is set in user answers" in pending
      }

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
                assert(Company.getFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }

        "for International company when" - {

          val specParams = Seq[(String, Settable[_])](
            ("'company name'", CompanyNamePage),
            ("'is address in the UK'", CheckCompanyRegisteredOfficeAddressPage),
            ("'International address'", CompanyRegisteredOfficeUkAddressPage) // TODO change to International address page when implemented
          )

          specParams foreach { x =>

            s"${x._1} is missing from user answers" in {
              forAll(genCompanyName, arbitraryInternationalAddress.arbitrary) { (name, address) =>
                val userAnswers = completeInternationalUserAnswers(name, address).remove(x._2).success.value
                assert(Company.getFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }
      }
    }

    "when .getParentCompanyFromUserAnswers is called" - {

      "must return a company" - {

        "when a UK Company is set in user answers" in {
          forAll(genCompanyName, arbitraryUkAddress.arbitrary) { (name, address) =>
            Company.getParentCompanyFromUserAnswers(completeUKUserAnswers(name, address, isParentCompany = true)).value mustBe Company(name, address)
          }
        }

        "when an International Company is set in user answers" in {
          forAll(genCompanyName, arbitraryInternationalAddress.arbitrary) { (name, address) =>
            Company.getParentCompanyFromUserAnswers(completeInternationalUserAnswers(name, address, isParentCompany = true)).value mustBe Company(name, address)
          }
        }
      }

      "must return None" - {

        "for UK company when" - {

          val specParams = Seq[(String, Settable[_])](
            ("'company name'", UltimateParentCompanyNamePage),
            ("'is address in the UK'", CheckUltimateGlobalParentCompanyInUkPage),
            ("'UK address'", UltimateParentCompanyUkAddressPage)
          )

          specParams foreach { x =>

            s"${x._1} is missing from user answers" in {
              forAll(genCompanyName, arbitraryInternationalAddress.arbitrary) { (name, address) =>
                val userAnswers = completeInternationalUserAnswers(name, address).remove(x._2).success.value
                assert(Company.getParentCompanyFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }

        "for International company when" - {

          val specParams = Seq[(String, Settable[_])](
            ("'company name'", UltimateParentCompanyNamePage),
            ("'is address in the UK'", CheckUltimateGlobalParentCompanyInUkPage),
            ("'International address'", UltimateParentCompanyInternationalAddressPage)
          )

          specParams foreach { x =>

            s"${x._1} is missing from user answers" in {
              forAll(genCompanyName, arbitraryInternationalAddress.arbitrary) { (name, address) =>
                val userAnswers = completeInternationalUserAnswers(name, address).remove(x._2).success.value
                assert(Company.getParentCompanyFromUserAnswers(userAnswers).isEmpty)
              }
            }
          }
        }
      }
    }
  }
}
