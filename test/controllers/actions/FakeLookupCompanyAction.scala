package controllers.actions

import controllers.actions.LookupCompanyAction.LookupCompanyDataRequest
import models.DataModel._
import models.requests.DataRequest
import models.{Company, CompanyRegWrapper, UkAddress}
import play.api.mvc.{PlayBodyParsers, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeLookupCompanyAction @Inject()(bodyParsers: PlayBodyParsers) extends LookupCompanyAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, LookupCompanyDataRequest[A]]] = {
    val companyRegWrapper: CompanyRegWrapper = {
      CompanyRegWrapper(Company(CompanyName("clark"), UkAddress(AddressLine("28 Clifford Aven"),None,None,None,Postcode("HG18 3RE"))))
    }

    val dataRequest = LookupCompanyDataRequest(request, request.userId, request.userAnswers, companyRegWrapper)

    Future.successful(Right(dataRequest))
  }

  override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
