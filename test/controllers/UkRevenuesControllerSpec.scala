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
import forms.UkRevenuesFormProvider
import models.{Company, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CompanyNamePage, CompanyRegisteredOfficeUkAddressPage, UkRevenuesPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.DigitalServicesTaxService
import views.html.UkRevenuesView

import scala.concurrent.Future

class UkRevenuesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new UkRevenuesFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val UkRevenuesRoute: String = routes.UkRevenuesController.onPageLoad(NormalMode).url

  "UkRevenues Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, UkRevenuesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkRevenuesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(UkRevenuesPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, UkRevenuesRoute)

        val view = application.injector.instanceOf[UkRevenuesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect when valid data is submitted" - {

      "and a company is matched from the backend and answer is 'yes'" in {

        val mockService = mock[DigitalServicesTaxService]
        val mockSessionRepository = mock[SessionRepository]

        val company = Company("Big Corp", UkAddress("123 Test Street", None, None, None, "TE5 3ST"))

        when(mockService.getCompany(any(), any())) thenReturn Future.successful(Some(company))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[DigitalServicesTaxService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, UkRevenuesRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockSessionRepository)
            .set(refEq(
              emptyUserAnswers
                .set(UkRevenuesPage, true).success.value
                .set(CompanyNamePage, company.name).success.value
                .set(CompanyRegisteredOfficeUkAddressPage, company.address.toCompanyRegisteredOfficeUkAddress).success.value,
              "lastUpdated"
            ))
        }
      }

      "and a company is matched from the backend and answer is 'no'" in {

        val mockService = mock[DigitalServicesTaxService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockService.getCompany(any(), any())) thenReturn Future.successful(Some(
          Company("Big Corp", UkAddress("123 Test Street", None, None, None, "TE5 3ST"))
        ))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[DigitalServicesTaxService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, UkRevenuesRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockSessionRepository)
            .set(refEq(emptyUserAnswers.set(UkRevenuesPage, false).success.value, "lastUpdated"))
        }
      }

      "and no match is found from the backend" in {

        val mockService = mock[DigitalServicesTaxService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockService.getCompany(any(), any())) thenReturn Future.successful(None)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[DigitalServicesTaxService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, UkRevenuesRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockSessionRepository).set(refEq(UserAnswers("id").set(UkRevenuesPage, true).success.value, "lastUpdated"))
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, UkRevenuesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UkRevenuesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
