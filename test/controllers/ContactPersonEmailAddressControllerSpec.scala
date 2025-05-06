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
import forms.ContactPersonEmailAddressFormProvider
import models.{ContactPersonName, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactPersonEmailAddressPage, ContactPersonNamePage}
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ContactPersonEmailAddressView

import scala.concurrent.Future

class ContactPersonEmailAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val contactPersonEmailAddressRoute: String =
    routes.ContactPersonEmailAddressController.onPageLoad(NormalMode).url

  val formProvider = new ContactPersonEmailAddressFormProvider()
  val form         = formProvider()

  val contactName: ContactPersonName   = ContactPersonName("Harry", "Spark")
  val userAnswersWithName: UserAnswers =
    UserAnswers(userAnswersId).set(ContactPersonNamePage, contactName).success.value
  val givenEmailAddress                = "harryspark@gmail.com"

  "ContactPersonEmailAddress controller" - {
    "must return OK and the correct view for a GET" - {
      "when contact name is set in user answers" in {
        val application = applicationBuilder(userAnswers = Option(userAnswersWithName)).build()

        running(application) {
          val request = FakeRequest(GET, contactPersonEmailAddressRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ContactPersonEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, contactName.fullName, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "when Contact name is not set in user answers" in {

        val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, contactPersonEmailAddressRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Option(routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithName.set(ContactPersonEmailAddressPage, givenEmailAddress).success.value

      val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, contactPersonEmailAddressRoute)

        val view = application.injector.instanceOf[ContactPersonEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(givenEmailAddress), contactName.fullName, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Option(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, contactPersonEmailAddressRoute)
            .withFormUrlEncodedBody(("contact-email-address", givenEmailAddress))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery when invalid data is submitted and no contact name is set in user answers" in {

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, contactPersonEmailAddressRoute)
            .withFormUrlEncodedBody(("contact-email-address", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Option(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, contactPersonEmailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, contactPersonEmailAddressRoute)
            .withFormUrlEncodedBody(("contact-email-address", givenEmailAddress))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
