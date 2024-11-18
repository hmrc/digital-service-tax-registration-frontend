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
import forms.ConfirmCompanyDetailsFormProvider
import models.{Company, Location, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CompanyNamePage, CompanyRegisteredOfficeUkAddressPage, ConfirmCompanyDetailsPage, UkRevenuesPage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ConfirmCompanyDetailsView

import scala.concurrent.Future

class ConfirmCompanyDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ConfirmCompanyDetailsFormProvider()
  val form         = formProvider()

  lazy val confirmCompanyDetailsRoute: String = routes.ConfirmCompanyDetailsController.onPageLoad(NormalMode).url

  val company: Company = Company("Big Corp", UkAddress("123 Test Street", None, None, None, "TE5 3ST"))

  def location(app: Application): Location = app.injector.instanceOf[Location]

  "ConfirmCompanyDetails Controller" - {

    val userAnswers = UserAnswers(userAnswersId)
      .set(CompanyNamePage, company.name)
      .success
      .value
      .set(CompanyRegisteredOfficeUkAddressPage, company.address.toCompanyRegisteredOfficeUkAddress)
      .success
      .value

    "must return OK and the correct view for a GET" in {

      val application: Application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmCompanyDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmCompanyDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(company, location(application), form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = userAnswers.set(ConfirmCompanyDetailsPage, true).success.value

      val application: Application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, confirmCompanyDetailsRoute)

        val view = application.injector.instanceOf[ConfirmCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(company, location(application), form.fill(true), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Check Company Registered Office Address page if company is not set in cache" - {

      val userAnswersWithUKRevenues = emptyUserAnswers.set(UkRevenuesPage, true).success.value

      "during GET request" in {

        val application: Application = applicationBuilder(Some(userAnswersWithUKRevenues)).build()

        running(application) {
          val request = FakeRequest(GET, confirmCompanyDetailsRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CheckCompanyRegisteredOfficeAddressController
            .onPageLoad(NormalMode)
            .url
        }
      }

      "during POST request with invalid body" in {

        val application: Application = applicationBuilder(Some(userAnswersWithUKRevenues)).build()

        running(application) {
          val request =
            FakeRequest(POST, confirmCompanyDetailsRoute)
              .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CheckCompanyRegisteredOfficeAddressController
            .onPageLoad(NormalMode)
            .url
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmCompanyDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmCompanyDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ConfirmCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(company, location(application), boundForm, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmCompanyDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmCompanyDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
