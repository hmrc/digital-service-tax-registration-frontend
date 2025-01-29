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

package controllers

import base.SpecBase
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction, FakeIdentifierAction}
import models.{Location, Registration, UserAnswers}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers, Injecting}
import services.{CheckYourAnswersService, DigitalServicesTaxService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with Injecting {

  val childCompanyName  = "Child Company Ltd"
  val parentCompanyName = "Parent Company Inc"

  val userAnswers: UserAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "globalRevenues"                            -> JsTrue,
        "ukRevenues"                                -> JsTrue,
        "checkCompanyRegisteredOfficeAddress"       -> JsTrue,
        "checkCompanyRegisteredOfficePostcode"      -> JsString("TE5 5ST"),
        "checkUtr"                                  -> JsTrue,
        "corporationTaxEnterUtr"                    -> JsString("1234567890"),
        "companyName"                               -> JsString(childCompanyName),
        "companyRegisteredOfficeUkAddress"          -> JsObject(
          Map(
            "buildingorstreet" -> JsString("123 Test Street"),
            "postcode"         -> JsString("TE5 5ST")
          )
        ),
        "companyContactAddress"                     -> JsTrue,
        "checkIfGroup"                              -> JsTrue,
        "ultimateParentCompanyName"                 -> JsString(parentCompanyName),
        "checkUltimateGlobalParentCompanyInUk"      -> JsFalse,
        "ultimateParentCompanyInternationalAddress" -> JsObject(
          Map(
            "line1"   -> JsString("678 Big Street"),
            "line2"   -> JsString("New York"),
            "country" -> JsObject(
              Map(
                "name" -> JsString("United States"),
                "code" -> JsString("US"),
                "type" -> JsString("country")
              )
            )
          )
        ),
        "contactPersonName"                         -> JsObject(
          Map(
            "firstName" -> JsString("John"),
            "lastName"  -> JsString("Smith")
          )
        ),
        "contactPersonPhoneNumber"                  -> JsString("0044 808 157 0192"),
        "contactPersonEmailAddress"                 -> JsString("john.smith@gmail.com"),
        "liabilityStartDate"                        -> JsString("2022-12-12"),
        "accountingPeriodEndDate"                   -> JsString("2023-12-11")
      )
    )

  implicit lazy val app: Application = applicationBuilder(Some(userAnswers)).build()

  val mockCyaService: CheckYourAnswersService   = mock[CheckYourAnswersService]
  val mockDstService: DigitalServicesTaxService = mock[DigitalServicesTaxService]

  val view: CheckYourAnswersView = inject[CheckYourAnswersView]

  val controller = new CheckYourAnswersController(
    inject[MessagesApi],
    inject[FakeIdentifierAction],
    new FakeDataRetrievalAction(Some(userAnswers)),
    new DataRequiredActionImpl,
    Helpers.stubMessagesControllerComponents(),
    mockCyaService,
    mockDstService,
    view
  )

  val location: Location = inject[Location]

  implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

  implicit val messages: Messages = messages(app)

  def summaryLists(implicit messages: Messages): Map[String, SummaryList] =
    Map[String, SummaryList](
      "responsibleMember"       -> SummaryList(
        Seq(
          CompanyNameSummary.row(userAnswers),
          CompanyRegisteredOfficeUkAddressSummary.row(userAnswers),
          ContactUkAddressSummary.row(userAnswers),
          InternationalContactAddressSummary.row(userAnswers, location),
          CheckIfGroupSummary.row(userAnswers)
        ).flatten
      ),
      "ultimateGlobalParent"    -> SummaryList(
        Seq(
          UltimateParentCompanyNameSummary.row(userAnswers),
          UltimateParentCompanyUkAddressSummary.row(userAnswers),
          UltimateParentCompanyInternationalAddressSummary.row(userAnswers, location)
        ).flatten
      ),
      "contactPersonDetails"    -> SummaryList(
        Seq(
          ContactPersonNameSummary.row(userAnswers),
          ContactPersonPhoneNumberSummary.row(userAnswers),
          ContactPersonEmailAddressSummary.row(userAnswers)
        ).flatten
      ),
      "accountingPeriodDetails" -> SummaryList(
        Seq(
          LiabilityStartDateSummary.row(userAnswers),
          AccountingPeriodEndDateSummary.row(userAnswers)
        ).flatten
      )
    )

  override def beforeEach(): Unit = {
    reset(mockDstService)
    reset(mockCyaService)
    super.beforeEach()
  }

  "CheckYourAnswers Controller" - {

    "when handling a GET request" - {

      "must return OK and the correct view for a GET" in {

        implicit val request = DataRequest(FakeRequest(), userAnswersId, userAnswers)

        when(mockCyaService.getSummaryForView(any(), any()))
          .thenReturn(Future.successful(Some(summaryLists)))

        when(mockCyaService.getChildCompanyName(any()))
          .thenReturn(Future.successful(Some(childCompanyName)))

        when(mockCyaService.getParentCompanyName(any()))
          .thenReturn(Future.successful(Some(parentCompanyName)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryLists, childCompanyName, Some(parentCompanyName)).toString
      }

      "must throw an error" - {

        "when service fails to return a summary list" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          when(mockCyaService.getSummaryForView(any(), any()))
            .thenReturn(Future.successful(None))

          when(mockCyaService.getChildCompanyName(any()))
            .thenReturn(Future.successful(Some(childCompanyName)))

          when(mockCyaService.getParentCompanyName(any()))
            .thenReturn(Future.successful(Some(parentCompanyName)))

          val result = controller.onPageLoad()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }

        "when service fails to return the child company name" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          when(mockCyaService.getSummaryForView(any(), any()))
            .thenReturn(Future.successful(Some(summaryLists)))

          when(mockCyaService.getChildCompanyName(any()))
            .thenReturn(Future.successful(None))

          when(mockCyaService.getParentCompanyName(any()))
            .thenReturn(Future.successful(Some(parentCompanyName)))

          val result = controller.onPageLoad()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }

        "when service fails to return both summary lists and the child company name" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          when(mockCyaService.getSummaryForView(any(), any()))
            .thenReturn(Future.successful(None))

          when(mockCyaService.getChildCompanyName(any()))
            .thenReturn(Future.successful(None))

          when(mockCyaService.getParentCompanyName(any()))
            .thenReturn(Future.successful(Some(parentCompanyName)))

          val result = controller.onPageLoad()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "handling a post request" - {

      "must redirect" - {

        "to registration complete when submission is successful" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          val mockRegistration = mock[Registration]

          when(mockCyaService.buildRegistration(any(), any()))
            .thenReturn(Future.successful(Some(mockRegistration)))

          when(mockDstService.submitRegistration(eqTo(mockRegistration))(any(), any()))
            .thenReturn(Future.successful(HttpResponse(OK, "{}")))

          val result = controller.onSubmit()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.RegistrationController.registrationComplete.url
        }

        "to register action method" - {

          "when a non-200 response is received upon submission" in {

            implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

            val mockRegistration = mock[Registration]

            when(mockCyaService.buildRegistration(any(), any()))
              .thenReturn(Future.successful(Some(mockRegistration)))

            when(mockDstService.submitRegistration(eqTo(mockRegistration))(any(), any()))
              .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "{}")))

            val result = controller.onSubmit()(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.RegistrationController.registerAction.url
          }

          "when registration cannot be built from user answers" in {

            implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

            when(mockCyaService.buildRegistration(any(), any()))
              .thenReturn(Future.successful(None))

            val result = controller.onSubmit()(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.RegistrationController.registerAction.url

            verifyNoInteractions(mockDstService)
          }
        }
      }
    }
  }
}
