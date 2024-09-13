package controllers.actions

import base.SpecBase
import models.DataModel.UTR
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbString
import org.scalatestplus.mockito.MockitoSugar
import pages.CorporationTaxEnterUtrPage
import play.test.Helpers.fakeRequest
import uk.gov.hmrc.http.HeaderCarrier

class LookupCompanyActionSpec extends SpecBase with MockitoSugar {

  class Harness(action: LookupCompanyAction) {
    def onPageLoad(): LookupCompanyAction = action
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "LookupCompanyAction" - {

    "lookup Company" - {

      "should lookup a company successfully by utr and postcode" - {
        val utr = Arbitrary.arbitrary[UTR].sample.value

      }


      "NEED TO TYPE SOMETHING FOR RIGHT" - {

      }

      "NEED TO TYPE SOMETHING FOR WRONG" - {

      }

      "NEED TO TYPE SOMETHING FOR WRONG" - {

      }

      "must redirect the session to journey recovery page" - {
        val userAnswers = mock[UserAnswers]

        when(userAnswers.get(CorporationTaxEnterUtrPage)).thenReturn(None)

        val request = DataRequest(fakeRequest, "userId", userAnswers)

        val result = LookupCompanyAction.refine(request).futureValue



//        val application = applicationBuilder(userAnswers = None).build()
//        val utr = {
//          Arbitrary.arbitrary[UTR].sample.value
//        }
//        val postcode = Arbitrary.arbitrary[].sample.value
//        val escaped = postcode.replaceAll("\\s+", "")
//
//        running(application) {
//          val request =
//            FakeRequest(POST, s"/digital-services-tax/lookup-company/$utr/$escaped")
//              .withFormUrlEncodedBody(("value", "true"))
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad()
//        }


      }

    }



  }

}
