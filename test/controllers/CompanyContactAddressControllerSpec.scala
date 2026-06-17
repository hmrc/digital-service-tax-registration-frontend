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
import forms.CompanyContactAddressFormProvider
import models.{InternationalAddress, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CheckCompanyRegisteredOfficeAddressPage, CompanyContactAddressPage, CompanyRegisteredOfficeInternationalAddressPage, CompanyRegisteredOfficeUkAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.CompanyContactAddressView

import scala.concurrent.Future

class CompanyContactAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                                   = new CompanyContactAddressFormProvider()
  val form: Form[Boolean]                            = formProvider()
  val companyOfficeRegisterEmptyAddress              = UkAddress("", Some(""), Some(""), Some(""), "")
  val companyOfficeRegisterEmptyInternationalAddress = InternationalAddress("", Some(""), Some(""), Some(""), "unknown")
  val companyOfficeRegisterInformationAddress        = UkAddress("kirby", Some(""), Some("london"), Some("essex"), "SW2 5IQ")

  private def addressLines(address: UkAddress): Seq[String] =
    Seq(address.line1) ++ address.line2.toSeq ++ address.line3.toSeq ++ address.line4.toSeq ++ Seq(address.postalCode)

  private def addressLinesInternational(address: InternationalAddress): Seq[String] =
    Seq(address.line1) ++ address.line2.toSeq ++ address.line3.toSeq ++ address.line4.toSeq ++ Seq(address.countryCode)

  lazy val companyContactAddressRoute: String = routes.CompanyContactAddressController.onPageLoad(NormalMode).url

  "CompanyContactAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyRegisteredOfficeUkAddressPage, companyOfficeRegisterEmptyAddress)
        .get
        .set(CheckCompanyRegisteredOfficeAddressPage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, addressLines(companyOfficeRegisterEmptyAddress), NormalMode)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view if International Address is provided for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyRegisteredOfficeInternationalAddressPage, companyOfficeRegisterEmptyInternationalAddress)
        .get
        .set(CheckCompanyRegisteredOfficeAddressPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          addressLinesInternational(companyOfficeRegisterEmptyInternationalAddress),
          NormalMode
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyRegisteredOfficeUkAddressPage, companyOfficeRegisterInformationAddress)
        .success
        .value
        .set(CheckCompanyRegisteredOfficeAddressPage, true)
        .get
        .set(CompanyContactAddressPage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyContactAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CompanyContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(true),
          addressLines(companyOfficeRegisterInformationAddress),
          NormalMode
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CompanyRegisteredOfficeUkAddressPage, companyOfficeRegisterEmptyAddress)
        .get
        .set(CheckCompanyRegisteredOfficeAddressPage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CompanyContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, addressLines(companyOfficeRegisterEmptyAddress), NormalMode)(
          using
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET when" - {

      "no address can be found" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(CompanyContactAddressPage, true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, companyContactAddressRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "no address can be found for for true in CheckCompanyRegisteredOfficeAddress" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(CheckCompanyRegisteredOfficeAddressPage, true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, companyContactAddressRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "no address can be found for for false in CheckCompanyRegisteredOfficeAddress" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(CheckCompanyRegisteredOfficeAddressPage, false)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, companyContactAddressRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, companyContactAddressRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyContactAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
