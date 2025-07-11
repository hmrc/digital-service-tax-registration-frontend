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

package controllers

import base.SpecBase
import forms.AccountingPeriodEndDateFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.AccountingPeriodEndDateView

import java.time.LocalDate
import scala.concurrent.Future

class AccountingPeriodEndDateControllerSpec extends SpecBase with MockitoSugar {
  implicit val messages: Messages = stubMessages()

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider                       = new AccountingPeriodEndDateFormProvider()
  val form: Form[LocalDate]              = formProvider(isGroup = true, LocalDate.now)
  lazy val apRoute: String               = routes.AccountingPeriodEndDateController.onPageLoad(NormalMode).url
  private val userAnswers                =
    UserAnswers(userAnswersId, Json.obj("checkIfGroup" -> true, "liabilityStartDate" -> LocalDate.of(2022, 6, 6)))
  private val userAnswersWithAp          =
    userAnswers.copy(data = userAnswers.data.+(("accountingPeriodEndDate", Json.toJson(LocalDate.of(2022, 9, 9)))))
  private val accountingPeriodEndDateKey = "accounting-period-end-date"

  "AccountingPeriodEndDateController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, apRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountingPeriodEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, isGroup = true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Option(userAnswersWithAp)).build()

      running(application) {
        val request = FakeRequest(GET, apRoute)

        val view = application.injector.instanceOf[AccountingPeriodEndDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LocalDate.of(2022, 9, 9)), NormalMode, isGroup = true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Option(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, apRoute)
            .withFormUrlEncodedBody(
              s"$accountingPeriodEndDateKey.day"   -> "9",
              s"$accountingPeriodEndDateKey.month" -> "9",
              s"$accountingPeriodEndDateKey.year"  -> "2022"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, apRoute).withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map(accountingPeriodEndDateKey -> ""))

        val view = application.injector.instanceOf[AccountingPeriodEndDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, isGroup = true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "if 'Check If Group' answer is not in cache" in {

        val application = applicationBuilder(Option(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, apRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, apRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "must redirect to Journey Recovery for a POST" - {

      "if 'Check If Group' answer is not in cache" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Option(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, apRoute)
              .withFormUrlEncodedBody(
                s"$accountingPeriodEndDateKey.day"   -> "9",
                s"$accountingPeriodEndDateKey.month" -> "9",
                s"$accountingPeriodEndDateKey.year"  -> "2022"
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, apRoute).withFormUrlEncodedBody(
            s"$accountingPeriodEndDateKey.day"   -> "9",
            s"$accountingPeriodEndDateKey.month" -> "9",
            s"$accountingPeriodEndDateKey.year"  -> "2022"
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
