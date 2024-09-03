package controllers.actions

import connectors.DSTConnector
import controllers.actions.LookupCompanyAction.LookupCompanyDataRequest
import controllers.routes
import models.DataModel.{Postcode, UTR}
import models.{CompanyRegWrapper, UserAnswers}
import models.requests.DataRequest
import pages.{CheckCompanyRegisteredOfficePostcodePage, CorporationTaxEnterUtrPage}
import play.api.mvc.{ActionRefiner, Request, Result, WrappedRequest}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupCompanyAction @Inject()(dstConnector: DSTConnector)(implicit val executionContext: ExecutionContext, headerCarrier: HeaderCarrier) extends ActionRefiner[DataRequest, LookupCompanyDataRequest] {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, LookupCompanyDataRequest[A]]] = {
    request.userAnswers.get(CorporationTaxEnterUtrPage) match {
      case Some(utr) => request.userAnswers.get(CheckCompanyRegisteredOfficePostcodePage) match {
        case Some(postcode) => dstConnector.lookupCompany(UTR(utr), Postcode(postcode)).map {
            case Some(companyRegWrapper) => Right(LookupCompanyDataRequest(request, request.userId, request.userAnswers, companyRegWrapper))
            case None => Left(routes.JourneyRecoveryController.onPageLoad())
          }
        case None => Future.successful(Left(routes.JourneyRecoveryController.onPageLoad()))
      }
      case None => Future.successful(Left(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}

object LookupCompanyAction {

  case class LookupCompanyDataRequest[A](request: Request[A], userId: String, userAnswers: UserAnswers, companyRegWrapper: CompanyRegWrapper) extends WrappedRequest[A](request)

}
