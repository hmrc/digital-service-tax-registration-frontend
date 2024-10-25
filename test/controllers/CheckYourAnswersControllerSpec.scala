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
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import services.CheckYourAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

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

  def summaryLists(implicit messages: Messages): Map[String, SummaryList] =
    Map[String, SummaryList](
      "responsibleMember"       -> SummaryList(
        Seq(
          CompanyNameSummary.row(userAnswers),
          CompanyRegisteredOfficeUkAddressSummary.row(userAnswers),
          ContactUkAddressSummary.row(userAnswers),
          InternationalContactAddressSummary.row(userAnswers),
          CheckIfGroupSummary.row(userAnswers)
        ).flatten
      ),
      "ultimateGlobalParent"    -> SummaryList(
        Seq(
          UltimateParentCompanyNameSummary.row(userAnswers),
          UltimateParentCompanyUkAddressSummary.row(userAnswers),
          UltimateParentCompanyInternationalAddressSummary.row(userAnswers)
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

  implicit lazy val app: Application = applicationBuilder(Some(userAnswers)).build()

  val mockService: CheckYourAnswersService = mock[CheckYourAnswersService]

  val view: CheckYourAnswersView = app.injector.instanceOf[CheckYourAnswersView]

  val controller = new CheckYourAnswersController(
    app.injector.instanceOf[MessagesApi],
    app.injector.instanceOf[FakeIdentifierAction],
    new FakeDataRetrievalAction(Some(userAnswers)),
    new DataRequiredActionImpl,
    Helpers.stubMessagesControllerComponents(),
    mockService,
    view
  )

  implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

  implicit val messages: Messages = messages(app)

  "CheckYourAnswers Controller" - {

    "when handling a GET request" - {

      "must return OK and the correct view for a GET" in {

        implicit val request = DataRequest(FakeRequest(), userAnswersId, userAnswers)

        when(mockService.getSummaryForView(any(), any()))
          .thenReturn(Future.successful(Some(summaryLists)))

        when(mockService.getChildCompanyName(any()))
          .thenReturn(Future.successful(Some(childCompanyName)))

        when(mockService.getParentCompanyName(any()))
          .thenReturn(Future.successful(Some(parentCompanyName)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryLists, childCompanyName, Some(parentCompanyName)).toString
      }

      "must throw an error" - {

        "when service fails to return a summary list" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          when(mockService.getSummaryForView(any(), any()))
            .thenReturn(Future.successful(None))

          when(mockService.getChildCompanyName(any()))
            .thenReturn(Future.successful(Some(childCompanyName)))

          when(mockService.getParentCompanyName(any()))
            .thenReturn(Future.successful(Some(parentCompanyName)))

          val result = controller.onPageLoad()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }

        "when service fails to return the child company name" in {

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), userAnswersId, userAnswers)

          when(mockService.getSummaryForView(any(), any()))
            .thenReturn(Future.successful(Some(summaryLists)))

          when(mockService.getChildCompanyName(any()))
            .thenReturn(Future.successful(None))

          when(mockService.getParentCompanyName(any()))
            .thenReturn(Future.successful(Some(parentCompanyName)))

          val result = controller.onPageLoad()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "handling a post request" in pending
  }
}
