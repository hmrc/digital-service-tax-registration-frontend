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

package navigation

import controllers.routes
import models.{NormalMode, UserAnswers}
import pages.{GlobalRevenuesPage, UkRevenuesPage}
import play.api.mvc.Call

trait NavigationUtils {
  def globalRevenues(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(GlobalRevenuesPage).map {
      case true => routes.UkRevenuesController.onPageLoad(NormalMode)
      case false => routes.GlobalRevenuesNotEligibleController.onPageLoad()
    }
  }

  def ukRevenues(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(UkRevenuesPage).map {
          case true => routes.CheckCompanyRegisteredOfficeAddressController.onPageLoad(NormalMode)
          case false => routes.UkRevenueNotEligibleController.onPageLoad()
    }
  }
  def checkCompanyRegisteredOfficeAddress(userAnswers: UserAnswers): Option[Call] = {
    userAnswers.get(UkRevenuesPage).map {
      case true => routes.GlobalRevenuesController.onPageLoad(NormalMode)
      case false => routes.GlobalRevenuesController.onPageLoad(NormalMode)
    }
  }
}