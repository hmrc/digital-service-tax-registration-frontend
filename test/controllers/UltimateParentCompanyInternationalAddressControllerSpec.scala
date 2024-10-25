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
import forms.InternationalAddressFormProvider
import models.{Country, InternationalAddress, NormalMode, UltimateParentCompanyUkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{UltimateParentCompanyInternationalAddressPage, UltimateParentCompanyNamePage, UltimateParentCompanyUkAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import views.html.UltimateParentCompanyInternationalAddressView

import scala.concurrent.Future
import scala.util.Try

class UltimateParentCompanyInternationalAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val country: Country         = Country("Andorra", "AD", "country")
  private val locations: Seq[Country]  = Seq(country)
  val formProvider                     = new InternationalAddressFormProvider()
  val form: Form[InternationalAddress] = formProvider(locations)

  private val selectOptions: Seq[SelectItem]    = Seq(
    SelectItem(Some(""), ""),
    SelectItem(value = Some("AL"), text = "Albania"),
    SelectItem(value = Some("DZ"), text = "Algeria"),
    SelectItem(value = Some("AD"), text = "Andorra", selected = true)
  )
  private val ultimateParentCompanyName: String = "UltimateParentName"

  lazy val ultimateParentCompanyInternationalAddressRoute: String =
    routes.UltimateParentCompanyInternationalAddressController.onPageLoad(NormalMode).url

  val userAnswers: UserAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      UltimateParentCompanyInternationalAddressPage.toString -> Json.obj(
        "line1"   -> "value 1",
        "line2"   -> "value 2",
        "line3"   -> "value 3",
        "line4"   -> "value 4",
        "country" -> Json.toJson(country)
      ),
      UltimateParentCompanyNamePage.toString                 -> JsString(ultimateParentCompanyName)
    )
  )

  "UltimateParentCompanyInternationalAddress Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(UltimateParentCompanyNamePage, ultimateParentCompanyName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyInternationalAddressRoute)

        val view = application.injector.instanceOf[UltimateParentCompanyInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, selectOptions, ultimateParentCompanyName, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to there is a problem page when UltimateParentCompanyNamePage is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyInternationalAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyInternationalAddressRoute)

        val view = application.injector.instanceOf[UltimateParentCompanyInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(InternationalAddress("value 1", Some("value 2"), Some("value 3"), Some("value 4"), country)),
            selectOptions,
            ultimateParentCompanyName,
            NormalMode
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
          FakeRequest(POST, ultimateParentCompanyInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "AD"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect and remove UK address if it is set in User Answers" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockUserAnswers       = mock[UserAnswers]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswers.removeIfSet(any())(any()))
        .thenCallRealMethod()

      when(mockUserAnswers.get(eqTo(UltimateParentCompanyUkAddressPage))(any()))
        .thenReturn(Some(UltimateParentCompanyUkAddress("123 Test Street", postcode = "TE5 5ST")))

      when(mockUserAnswers.remove(eqTo(UltimateParentCompanyUkAddressPage)))
        .thenReturn(Try(mockUserAnswers))

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
          FakeRequest(POST, ultimateParentCompanyInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "AD"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswers).remove(eqTo(UltimateParentCompanyUkAddressPage))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers
        .set(UltimateParentCompanyNamePage, ultimateParentCompanyName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ultimateParentCompanyInternationalAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UltimateParentCompanyInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, selectOptions, ultimateParentCompanyName, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ultimateParentCompanyInternationalAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ultimateParentCompanyInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
