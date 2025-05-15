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
import forms.UltimateParentCompanyUkAddressFormProvider
import models.{InternationalAddress, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{UltimateParentCompanyInternationalAddressPage, UltimateParentCompanyNamePage, UltimateParentCompanyUkAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.UltimateParentCompanyUkAddressView

import scala.concurrent.Future
import scala.util.Try

class UltimateParentCompanyUkAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new UltimateParentCompanyUkAddressFormProvider()
  val form         = formProvider()

  lazy val ultimateParentCompanyUkAddressRoute =
    routes.UltimateParentCompanyUkAddressController.onPageLoad(NormalMode).url

  private val ultimateParentCompanyName: String = "UltimateParentName"

  private val addressLine = "123 Test Street"
  private val postcode    = "BT15GB"

  val userAnswers = UserAnswers(userAnswersId)
    .set(UltimateParentCompanyUkAddressPage, UkAddress(addressLine, None, None, None, postcode))
    .success
    .value
    .set(UltimateParentCompanyNamePage, ultimateParentCompanyName)
    .success
    .value

  "UltimateParentCompanyUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(UltimateParentCompanyNamePage, ultimateParentCompanyName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyUkAddressRoute)

        val view = application.injector.instanceOf[UltimateParentCompanyUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, ultimateParentCompanyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to there is a problem page when UltimateParentCompanyNamePage is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyUkAddressRoute)

        val view = application.injector.instanceOf[UltimateParentCompanyUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(UkAddress(addressLine, None, None, None, postcode)),
          NormalMode,
          ultimateParentCompanyName
        )(request, messages(application)).toString
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
          FakeRequest(POST, ultimateParentCompanyUkAddressRoute)
            .withFormUrlEncodedBody(("line1", addressLine), ("postcode", postcode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect and remove International address if it is set in User Answers" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockUserAnswers       = mock[UserAnswers]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswers.removeIfSet(any())(any()))
        .thenCallRealMethod()

      when(mockUserAnswers.get(eqTo(UltimateParentCompanyInternationalAddressPage))(any()))
        .thenReturn(
          Some(InternationalAddress(addressLine, None, None, None, "US"))
        )

      when(mockUserAnswers.set(any(), any())(any()))
        .thenReturn(Try(mockUserAnswers))

      when(mockUserAnswers.remove(eqTo(UltimateParentCompanyInternationalAddressPage)))
        .thenReturn(Try(mockUserAnswers))

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ultimateParentCompanyUkAddressRoute)
            .withFormUrlEncodedBody(("line1", addressLine), ("postcode", postcode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswers).remove(eqTo(UltimateParentCompanyInternationalAddressPage))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ultimateParentCompanyUkAddressRoute)
            .withFormUrlEncodedBody(("line1", "invalid value"))

        val boundForm = form.bind(Map("line1" -> "invalid value"))

        val view = application.injector.instanceOf[UltimateParentCompanyUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ultimateParentCompanyUkAddressRoute)
            .withFormUrlEncodedBody(("line1", addressLine), ("postcode", postcode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
