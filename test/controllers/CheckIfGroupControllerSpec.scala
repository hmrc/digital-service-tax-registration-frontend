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
import forms.CheckIfGroupFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.CheckIfGroupView

import scala.concurrent.Future
import scala.util.Success

class CheckIfGroupControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new CheckIfGroupFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val checkIfGroupRoute: String = routes.CheckIfGroupController.onPageLoad(NormalMode).url

  "CheckIfGroup Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkIfGroupRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckIfGroupView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(CheckIfGroupPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkIfGroupRoute)

        val view = application.injector.instanceOf[CheckIfGroupView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkIfGroupRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the correct page in Check Mode" - {

      "when user answers 'Yes'" in {

        val mockSessionRepository = mock[SessionRepository]

        val mockUserAnswers = mock[UserAnswers]

        when(mockUserAnswers.set(any(), any())(any()))
          .thenReturn(Success(mockUserAnswers))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(mockUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CheckIfGroupController.onPageLoad(CheckMode).url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockUserAnswers, times(0)).removeUltimateParentCompanyAnswers
          verify(mockUserAnswers, times(0)).remove(UltimateParentCompanyNamePage)
          verify(mockUserAnswers, times(0)).remove(CheckUltimateGlobalParentCompanyInUkPage)
          verify(mockUserAnswers, times(0)).remove(UltimateParentCompanyUkAddressPage)
          verify(mockUserAnswers, times(0)).remove(UltimateParentCompanyInternationalAddressPage)
        }
      }

      "and remove Ultimate Parent Company Answers when user answers 'No'" in {

        val mockSessionRepository = mock[SessionRepository]

        val mockUserAnswers = mock[UserAnswers]

        when(mockUserAnswers.set(any(), any())(any()))
          .thenReturn(Success(mockUserAnswers))

        when(mockUserAnswers.remove(any()))
          .thenReturn(Success(mockUserAnswers))

        when(mockUserAnswers.removeUltimateParentCompanyAnswers)
          .thenCallRealMethod()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(mockUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CheckIfGroupController.onPageLoad(CheckMode).url)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockUserAnswers).removeUltimateParentCompanyAnswers
          verify(mockUserAnswers).remove(UltimateParentCompanyNamePage)
          verify(mockUserAnswers).remove(CheckUltimateGlobalParentCompanyInUkPage)
          verify(mockUserAnswers).remove(UltimateParentCompanyUkAddressPage)
          verify(mockUserAnswers).remove(UltimateParentCompanyInternationalAddressPage)
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, checkIfGroupRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CheckIfGroupView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
