/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.auth

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.auth.IncorrectAccountCredRoleView

class IncorrectAccountCredRoleControllerSpec extends SpecBase {

  lazy val incorrectAccountCredRoleControllerRoute: String = routes.IncorrectAccountCredRoleController.onPageLoad().url

  "IncorrectAccountCredRole Controller" - {

    "must return UNAUTHORIZED and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, incorrectAccountCredRoleControllerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IncorrectAccountCredRoleView]

        status(result) mustEqual UNAUTHORIZED
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }
  }
}
