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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{RegistrationCompleteView, RegistrationSentView}

class RegistrationControllerSpec extends SpecBase {

  "Registration Controller" - {

    "must return OK and the correct registration complete view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationController.registrationComplete.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must return OK and the correct registration sent view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.RegistrationController.registrationSent("Fake Company", "fake.email@email.com").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("Fake Company", "fake.email@email.com")(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct start again view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationController.registerAction.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
