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

package services

import base.SpecBase
import controllers.routes
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.i18n.Messages
import play.api.libs.json.Writes
import play.api.mvc.AnyContent
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.HtmlFormat
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersServiceSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockLocation: Location = mock[Location]

  val serviceUnderTest = new CheckYourAnswersService(mockSessionRepository, mockLocation)

  val companyName = "Company Ltd"

  implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers)

  def mockUserAnswers[A](page: Settable[A], value: A)(implicit
    writes: Writes[A]
  ): OngoingStubbing[Future[Option[UserAnswers]]] = {
    reset(mockSessionRepository)
    when(mockSessionRepository.get(eqTo(userAnswersId)))
      .thenReturn(
        Future.successful(Some(UserAnswers(userAnswersId).set(page, value).success.value))
      )
  }

  def asAddressValue(lines: Seq[String]): Value =
    ValueViewModel(
      HtmlContent(
        "<ul class=\"govuk-list\">" + lines.map { line =>
          s"""<li>${HtmlFormat.escape(line)}<li>"""
        }.mkString + "</ul>"
      )
    )

  "CheckYourAnswersService" - {

    when(mockLocation.name(eqTo("US"))).thenReturn("United States")

    "when .getChildCompanyName is called" - {

      "must return the name if populated" in {

        mockUserAnswers(CompanyNamePage, companyName)

        serviceUnderTest.getChildCompanyName.futureValue mustBe Some(companyName)
      }

      "must return None" - {
        "when it is not set in user answers" in {

          when(mockSessionRepository.get(eqTo(userAnswersId)))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))

          serviceUnderTest.getChildCompanyName.futureValue mustBe None
        }

        "when user answers are not present" in {

          when(mockSessionRepository.get(eqTo(userAnswersId)))
            .thenReturn(Future.successful(None))

          serviceUnderTest.getChildCompanyName.futureValue mustBe None
        }
      }
    }

    "when .getParentCompanyName is called" - {

      "must return the name if populated" in {

        mockUserAnswers(UltimateParentCompanyNamePage, companyName)

        serviceUnderTest.getParentCompanyName.futureValue mustBe Some(companyName)
      }

      "must return None" - {
        "when it is not set in user answers" in {

          when(mockSessionRepository.get(eqTo(userAnswersId)))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))

          serviceUnderTest.getParentCompanyName.futureValue mustBe None
        }

        "when user answers are not present" in {

          when(mockSessionRepository.get(eqTo(userAnswersId)))
            .thenReturn(Future.successful(None))

          serviceUnderTest.getParentCompanyName.futureValue mustBe None
        }
      }
    }

    "when .getSummaryForView is called" - {

      implicit val messages: Messages = Helpers.stubMessages()

      def theCorrectSummaryListWithRow(rowKey: String, value: Value, href: String): SummaryList =
        SummaryList(
          Seq(
            SummaryListRowViewModel(
              messages(s"$rowKey.checkYourAnswersLabel"),
              value,
              Seq(
                ActionItem(
                  href,
                  Text(messages("site.change")),
                  Some(messages(s"$rowKey.change.hidden"))
                )
              )
            )
          )
        )

      "must return the Responsible Member Details summary list" - {

        "when Company Name is set in user answers" in {

          mockUserAnswers(CompanyNamePage, companyName)

          serviceUnderTest.getSummaryForView.futureValue.value("responsibleMember") mustBe
            theCorrectSummaryListWithRow(
              "companyName",
              ValueViewModel(companyName),
              routes.CompanyNameController.onPageLoad(CheckMode).url
            )
        }

        "when Company Registered Office Uk Address is set in user answers" in {

          val address = CompanyRegisteredOfficeUkAddress("123 Test Street", None, None, None, "TE55ST")

          mockUserAnswers(CompanyRegisteredOfficeUkAddressPage, address)

          serviceUnderTest.getSummaryForView.futureValue.value("responsibleMember") mustBe
            theCorrectSummaryListWithRow(
              "companyRegisteredOfficeUkAddress",
              asAddressValue(address.asAddressLines),
              routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(CheckMode).url
            )
        }

        "when Contact Uk Address is set in user answers" in {

          val address = ContactUkAddress("123 Test Street", None, None, None, "TE55ST")

          mockUserAnswers(ContactUkAddressPage, address)

          serviceUnderTest.getSummaryForView.futureValue.value("responsibleMember") mustBe
            theCorrectSummaryListWithRow(
              "contactUkAddress",
              asAddressValue(address.asAddressLines),
              routes.ContactUkAddressController.onPageLoad(CheckMode).url
            )
        }

        "when International Contact Address is set in user answers" in {

          val address =
            InternationalAddress("123 Test Street", None, None, None, "US")

          mockUserAnswers(InternationalContactAddressPage, address)

          serviceUnderTest.getSummaryForView.futureValue.value("responsibleMember") mustBe
            theCorrectSummaryListWithRow(
              "internationalContactAddress",
              asAddressValue(address.asAddressLines(mockLocation)),
              routes.InternationalContactAddressController.onPageLoad(CheckMode).url
            )
        }

        "when Check If Group is set in user answers" in {

          mockUserAnswers(CheckIfGroupPage, true)

          serviceUnderTest.getSummaryForView.futureValue.value("responsibleMember") mustBe
            theCorrectSummaryListWithRow(
              "checkIfGroup",
              ValueViewModel(messages("site.yes")),
              routes.CheckIfGroupController.onPageLoad(CheckMode).url
            )
        }
      }

      "must return the Ultimate Global Parent Company Details summary list" - {

        "when Ultimate Parent Company Name is set in user answers" in {

          mockUserAnswers(UltimateParentCompanyNamePage, companyName)

          serviceUnderTest.getSummaryForView.futureValue.value("ultimateGlobalParent") mustBe
            theCorrectSummaryListWithRow(
              "ultimateParentCompanyName",
              ValueViewModel(companyName),
              routes.UltimateParentCompanyNameController.onPageLoad(CheckMode).url
            )
        }

        "when Ultimate Parent Company Uk Address is set in user answers" in {

          val address = UltimateParentCompanyUkAddress("123 Test Street", postcode = "TE55ST")

          mockUserAnswers(UltimateParentCompanyUkAddressPage, address)

          serviceUnderTest.getSummaryForView.futureValue.value("ultimateGlobalParent") mustBe
            theCorrectSummaryListWithRow(
              "ultimateParentCompanyUkAddress",
              asAddressValue(address.asAddressLines),
              routes.UltimateParentCompanyUkAddressController.onPageLoad(CheckMode).url
            )
        }

        "when Ultimate Parent Company International Address is set in user answers" in {

          val address =
            InternationalAddress("123 Test Street", None, None, None, "US")

          mockUserAnswers(UltimateParentCompanyInternationalAddressPage, address)

          serviceUnderTest.getSummaryForView.futureValue.value("ultimateGlobalParent") mustBe
            theCorrectSummaryListWithRow(
              "ultimateParentCompanyInternationalAddress",
              asAddressValue(address.asAddressLines(mockLocation)),
              routes.UltimateParentCompanyInternationalAddressController.onPageLoad(CheckMode).url
            )
        }
      }

      "must not return the Ultimate Global Parent Company Details summary list" - {

        "when none of the corresponding values are set in user answers" in {

          reset(mockSessionRepository)
          when(mockSessionRepository.get(eqTo(userAnswersId)))
            .thenReturn(
              Future.successful(
                Some(
                  UserAnswers(userAnswersId)
                    .set(CompanyNamePage, companyName)
                    .success
                    .value
                    .set(ContactPersonNamePage, ContactPersonName("John", "Smith"))
                    .success
                    .value
                    .set(LiabilityStartDatePage, LocalDate.now())
                    .success
                    .value
                )
              )
            )

          Seq(
            "ultimateParentCompanyName",
            "ultimateParentCompanyUkAddress",
            "ultimateParentCompanyInternationalAddress"
          ) map { rowKey =>
            val rowKeys = serviceUnderTest.getSummaryForView.futureValue.value.keys
            rowKeys mustNot contain("ultimateGlobalParent")
          }
          serviceUnderTest.getSummaryForView.futureValue.value.size mustBe 3
        }
      }

      "must return the Contact Person Details summary list" - {

        "when Contact Person Name is set in user answers" in {

          val firstName = "John"
          val lastName  = "Smith"

          mockUserAnswers(ContactPersonNamePage, ContactPersonName(firstName, lastName))

          serviceUnderTest.getSummaryForView.futureValue.value("contactPersonDetails") mustBe
            theCorrectSummaryListWithRow(
              "contactPersonName",
              ValueViewModel(HtmlContent(s"$firstName $lastName")),
              routes.ContactPersonNameController.onPageLoad(CheckMode).url
            )
        }

        "when Contact Person Phone Number is set in user answers" in {

          val phoneNumber = "071234567890"

          mockUserAnswers(ContactPersonPhoneNumberPage, phoneNumber)

          serviceUnderTest.getSummaryForView.futureValue.value("contactPersonDetails") mustBe
            theCorrectSummaryListWithRow(
              "contactPersonPhoneNumber",
              ValueViewModel(phoneNumber),
              routes.ContactPersonPhoneNumberController.onPageLoad(CheckMode).url
            )
        }

        "when Contact Person Email Address is set in user answers" in {

          val email = "john.smith@gmail.com"

          mockUserAnswers(ContactPersonEmailAddressPage, email)

          serviceUnderTest.getSummaryForView.futureValue.value("contactPersonDetails") mustBe
            theCorrectSummaryListWithRow(
              "contactPersonEmailAddress",
              ValueViewModel(email),
              routes.ContactPersonEmailAddressController.onPageLoad(CheckMode).url
            )
        }
      }

      "must return the Accounting Period Details summary list" - {

        "when Liability Start Date Summary is set in user answers" in {

          val date: LocalDate = LocalDate.of(2022, 11, 1)

          mockUserAnswers(LiabilityStartDatePage, date)

          serviceUnderTest.getSummaryForView.futureValue.value("accountingPeriodDetails") mustBe
            theCorrectSummaryListWithRow(
              "liabilityStartDate",
              ValueViewModel("1 November 2022"),
              routes.LiabilityStartDateController.onPageLoad(CheckMode).url
            )
        }

        "when Accounting Period End Date is set in user answers" in {

          val date: LocalDate = LocalDate.of(2023, 2, 20)

          mockUserAnswers(AccountingPeriodEndDatePage, date)

          serviceUnderTest.getSummaryForView.futureValue.value("accountingPeriodDetails") mustBe
            theCorrectSummaryListWithRow(
              "accounting-period-end-date",
              ValueViewModel("20 February 2023"),
              routes.AccountingPeriodEndDateController.onPageLoad(CheckMode).url
            )
        }
      }
    }
  }
}
