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
import forms.CorporationTaxEnterUtrFormProvider
import models.{Company, CompanyRegWrapper, InternationalAddress, NormalMode, UkAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.DigitalServicesTaxService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.CorporationTaxEnterUtrView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CorporationTaxEnterUtrControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CorporationTaxEnterUtrFormProvider()
  val form         = formProvider()

  lazy val corporationTaxEnterUtrRoute = routes.CorporationTaxEnterUtrController.onPageLoad(NormalMode).url

  "CorporationTaxEnterUtr Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, corporationTaxEnterUtrRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorporationTaxEnterUtrView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(CorporationTaxEnterUtrPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, corporationTaxEnterUtrRoute)

        val view = application.injector.instanceOf[CorporationTaxEnterUtrView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockDstService        = mock[DigitalServicesTaxService]
      val validUtr              = "1234567890"
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(
        mockDstService.lookupCompany(any[String], any[String])(any[HeaderCarrier], any[ExecutionContext])
      ) thenReturn Future.successful(None)
      val userAnswers           = UserAnswers(userAnswersId).set(CheckCompanyRegisteredOfficePostcodePage, "NE11AA").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DigitalServicesTaxService].toInstance(mockDstService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", validUtr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must save Company Details from Lookup when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockDstService        = mock[DigitalServicesTaxService]
      val mockUserAnswers       = mock[UserAnswers]
      val validUtr              = "1234567890"
      val companyName           = "Company Name"
      val ukAddress             = UkAddress("line 1", None, None, None, "NE11AA")
      val companyRegWrapper     = CompanyRegWrapper(Company(companyName, ukAddress))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(
        mockDstService.lookupCompany(any[String], any[String])(any[HeaderCarrier], any[ExecutionContext])
      ) thenReturn Future.successful(Some(companyRegWrapper))

      when(mockUserAnswers.get(CheckCompanyRegisteredOfficePostcodePage)).thenReturn(Some("NE11AA"))
      when(mockUserAnswers.set(any(), any())(any())).thenReturn(Try(mockUserAnswers))

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DigitalServicesTaxService].toInstance(mockDstService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", validUtr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswers).set(ArgumentMatchers.eq(CompanyNamePage), ArgumentMatchers.eq(companyName))(any())
        verify(mockUserAnswers)
          .set(ArgumentMatchers.eq(CompanyRegisteredOfficeUkAddressPage), ArgumentMatchers.eq(ukAddress))(any())
        verify(mockUserAnswers)
          .set(ArgumentMatchers.eq(CheckCompanyRegisteredOfficeAddressPage), ArgumentMatchers.eq(true))(any())
      }
    }

    "must not save Company Details from Lookup when international address is returned" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockDstService        = mock[DigitalServicesTaxService]
      val mockUserAnswers       = mock[UserAnswers]
      val validUtr              = "1234567890"
      val companyName           = "Company Name"
      val internationalAddress  = InternationalAddress("line 1", None, None, None, "US")
      val companyRegWrapper     = CompanyRegWrapper(Company(companyName, internationalAddress))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(
        mockDstService.lookupCompany(any[String], any[String])(any[HeaderCarrier], any[ExecutionContext])
      ) thenReturn Future.successful(Some(companyRegWrapper))

      when(mockUserAnswers.get(CheckCompanyRegisteredOfficePostcodePage)).thenReturn(Some("NE11AA"))
      when(mockUserAnswers.set(any(), any())(any())).thenReturn(Try(mockUserAnswers))

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DigitalServicesTaxService].toInstance(mockDstService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", validUtr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswers, never()).set(ArgumentMatchers.eq(CompanyNamePage), any)(any())
        verify(mockUserAnswers, never()).set(ArgumentMatchers.eq(CompanyRegisteredOfficeUkAddressPage), any)(any())
        verify(mockUserAnswers, never()).set(ArgumentMatchers.eq(CheckCompanyRegisteredOfficeAddressPage), any)(any())
      }
    }

    "must redirect to CheckCompanyOfficeRegisteredPostcode page when postcode is missing" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockDstService        = mock[DigitalServicesTaxService]
      val validUtr              = "1234567890"
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId).set(CheckCompanyRegisteredOfficeAddressPage, true).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DigitalServicesTaxService].toInstance(mockDstService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", validUtr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckCompanyOfficeRegisteredPostcodeController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CorporationTaxEnterUtrView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, corporationTaxEnterUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, corporationTaxEnterUtrRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
