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
import forms.CompanyRegisteredOfficeUkAddressFormProvider
import models.{NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CompanyNamePage, CompanyRegisteredOfficeUkAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.CompanyRegisteredOfficeUkAddressView

import scala.concurrent.Future

class CompanyRegisteredOfficeUkAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val form: Form[UkAddress] = new CompanyRegisteredOfficeUkAddressFormProvider()()

  lazy val companyRegisteredOfficeUkAddressRoute: String =
    routes.CompanyRegisteredOfficeUkAddressController.onPageLoad(NormalMode).url

  val address: UkAddress = UkAddress("value 1", Some("value 2"), Some("value 3"), Some("value 4"), "G1 4PD")

  val companyName              = "Big Corp Inc"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(CompanyNamePage, companyName).success.value

  val prePopulatedUserAnswers: UserAnswers = userAnswers
    .set(CompanyRegisteredOfficeUkAddressPage, address)
    .success
    .value

  "CompanyRegisteredOfficeUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyRegisteredOfficeUkAddressRoute)

        val view = application.injector.instanceOf[CompanyRegisteredOfficeUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, companyName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(prePopulatedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyRegisteredOfficeUkAddressRoute)

        val view = application.injector.instanceOf[CompanyRegisteredOfficeUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(address),
          NormalMode,
          companyName
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegisteredOfficeUkAddressRoute)
            .withFormUrlEncodedBody(
              ("line1", address.line1),
              ("line2", address.line2.value),
              ("line3", address.line3.value),
              ("line4", address.line4.value),
              ("postcode", address.postalCode)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegisteredOfficeUkAddressRoute)
            .withFormUrlEncodedBody(("line1", "invalid value"))

        val boundForm = form.bind(Map("line1" -> "invalid value"))

        val view = application.injector.instanceOf[CompanyRegisteredOfficeUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, companyName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery" - {

      "for a GET" - {

        "if company name is not found in cache" in {

          val application = applicationBuilder(Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, companyRegisteredOfficeUkAddressRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, companyRegisteredOfficeUkAddressRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "for a POST when invalid data is submitted and" - {

        "if company name is not found in cache" in {

          val application = applicationBuilder(Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, companyRegisteredOfficeUkAddressRoute)
                .withFormUrlEncodedBody(("line1", "invalid value"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, companyRegisteredOfficeUkAddressRoute)
                .withFormUrlEncodedBody(("line1", "invalid value"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
