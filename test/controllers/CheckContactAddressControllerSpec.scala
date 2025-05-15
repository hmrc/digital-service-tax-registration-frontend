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
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CheckContactAddressPage, CompanyRegisteredOfficeUkAddressPage, ContactUkAddressPage, InternationalContactAddressPage}
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
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
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
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must update correct user answers and redirect to the next page " - {

      "when 'Yes' is submitted and Address is UK" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockUserAnswers       = mock[UserAnswers]

        val ukAddress = UkAddress("123 Test Street", None, None, None, "TE5 5ST")

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(any()))
          .thenReturn(Some(ukAddress))

        when(
          mockUserAnswers.get(eqTo(InternationalContactAddressPage))(any())
        ) // TODO change to correct page when it is implemented
          .thenReturn(None)

        when(mockUserAnswers.set(any(), any())(any()))
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

          verify(mockUserAnswers).set(eqTo(ContactUkAddressPage), eqTo(ukAddress))(any())
        }
      }
    }

    "must redirect to the next page when 'No' is submitted" in pendingUntilFixed {

      val mockSessionRepository = mock[SessionRepository]
      val mockUserAnswers       = mock[UserAnswers]

      val internationalAddress =
        InternationalAddress("123 Test Street", None, None, None, "US")

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswers.get(eqTo(CompanyRegisteredOfficeUkAddressPage))(any()))
        .thenReturn(None)

      when(
        mockUserAnswers.get(eqTo(InternationalContactAddressPage))(any())
      ) // TODO change to correct page when it is implemented
        .thenReturn(Some(internationalAddress))

      when(mockUserAnswers.set(any(), any())(any()))
        .thenCallRealMethod()
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
//        TODO - Verify correct page has been updated once it is implemented
//        verify(mockUserAnswers).set(eqTo(ContactUkAddressPage), eqTo(internationalAddress))
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
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
