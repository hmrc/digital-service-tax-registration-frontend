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
import forms.CheckContactAddressFormProvider
import models.{InternationalAddress, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.CheckContactAddressView

import scala.concurrent.Future
import scala.util.Try

class CheckContactAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new CheckContactAddressFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val checkContactAddressRoute: String = routes.CheckContactAddressController.onPageLoad(NormalMode).url

  "CheckContactAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkContactAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(using request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(CheckContactAddressPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkContactAddressRoute)

        val view = application.injector.instanceOf[CheckContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must update correct user answers and redirect to the next page " - {

      "when 'Yes' is submitted and Address is UK" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockUserAnswers       = mock[UserAnswers]

        val ukAddress = UkAddress("123 Test Street", None, None, None, "TE5 5ST")

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(using any()))
          .thenReturn(Some(ukAddress))

        when(
          mockUserAnswers.get(eqTo(CompanyRegisteredOfficeInternationalAddressPage))(using any())
        ).thenReturn(None)

        when(mockUserAnswers.set(any(), any())(using any()))
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
            FakeRequest(POST, checkContactAddressRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockUserAnswers).set(eqTo(ContactUkAddressPage), eqTo(ukAddress))(using any())
        }
      }

      "when 'Yes' is submitted and Address is International" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockUserAnswers       = mock[UserAnswers]

        val internationalAddress = InternationalAddress("123 Test Street", None, None, None, "US")

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(using any()))
          .thenReturn(None)

        when(
          mockUserAnswers.get(eqTo(CompanyRegisteredOfficeInternationalAddressPage))(using any())
        ).thenReturn(Some(internationalAddress))

        when(mockUserAnswers.set(any(), any())(using any()))
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
            FakeRequest(POST, checkContactAddressRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockUserAnswers).set(eqTo(InternationalContactAddressPage), eqTo(internationalAddress))(using any())
        }
      }

      "when 'Yes' is submitted and Neither UK nor International Address is stored" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockUserAnswers       = mock[UserAnswers]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(using any()))
          .thenReturn(None)

        when(
          mockUserAnswers.get(eqTo(CompanyRegisteredOfficeInternationalAddressPage))(using any())
        ).thenReturn(None)

        when(mockUserAnswers.set(any(), any())(using any()))
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
            FakeRequest(POST, checkContactAddressRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockUserAnswers, never()).set(eqTo(InternationalContactAddressPage), any)(using any())
          verify(mockUserAnswers, never()).set(eqTo(ContactUkAddressPage), any)(using any())
        }
      }
    }

    "must redirect to the next page when 'No' is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockUserAnswers       = mock[UserAnswers]

      val internationalAddress =
        InternationalAddress("123 Test Street", None, None, None, "US")

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(using any()))
        .thenReturn(None)

      when(
        mockUserAnswers.get(eqTo(CompanyRegisteredOfficeInternationalAddressPage))(using any())
      )
        .thenReturn(Some(internationalAddress))

      when(mockUserAnswers.set(any(), any())(using any()))
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
          FakeRequest(POST, checkContactAddressRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockUserAnswers).set(eqTo(CheckContactAddressPage), eqTo(false))(using any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, checkContactAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CheckContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(using request, messages(application)).toString
      }
    }
  }
}
