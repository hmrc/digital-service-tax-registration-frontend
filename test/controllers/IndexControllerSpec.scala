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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.FakeIdentifierAction
import models.NormalMode
import models.RegistrationJourneyState
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.test.Helpers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.Injecting
import services.DigitalServicesTaxService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar with Injecting {

  implicit lazy val app: Application = applicationBuilder(userAnswers = None).build()

  val mockService: DigitalServicesTaxService = mock[DigitalServicesTaxService]
  val mockAppConfig: FrontendAppConfig       = mock[FrontendAppConfig]

  val controller = new IndexController(
    Helpers.stubMessagesControllerComponents(),
    inject[FakeIdentifierAction],
    mockService,
    mockAppConfig
  )

  "Index Controller" - {

    "must redirect to global revenues for new registrations" in {

      when(mockService.getRegistrationJourneyState(using any(), any()))
        .thenReturn(Future.successful(RegistrationJourneyState.New))

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = controller.onPageLoad()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.GlobalRevenuesController.onPageLoad(NormalMode).url)
    }

    "must redirect to registration pending for pending registrations" in {

      when(mockService.getRegistrationJourneyState(using any(), any()))
        .thenReturn(Future.successful(RegistrationJourneyState.Pending))

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = controller.onPageLoad()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.RegistrationController.registrationPending().url)
    }

    "must redirect to existing registration frontend for existing registrations" in {

      when(mockService.getRegistrationJourneyState(using any(), any()))
        .thenReturn(Future.successful(RegistrationJourneyState.Existing))

      when(mockAppConfig.dstReturnsUrl)
        .thenReturn("http://localhost:8743/digital-services-tax-returns/")

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = controller.onPageLoad()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:8743/digital-services-tax-returns/")
    }
  }
}
