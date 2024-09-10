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

package forms

import forms.mappings.Mappings
import models.DataValues.DST_EPOCH
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages

import java.time.LocalDate
import javax.inject.Inject

class AccountingPeriodEndDateFormProvider @Inject() extends Mappings {
  private val accountingPeriodEndDateKey = "accounting-period-end-date"

  def apply(isGroup: Boolean, liabilityDate: LocalDate)(implicit messages: Messages): Form[LocalDate] = {
    val entityType = if (isGroup) "group" else "company"

    Form(
      accountingPeriodEndDateKey -> localDate(
        invalidKey = s"$accountingPeriodEndDateKey.invalid",
        allRequiredKey = s"$accountingPeriodEndDateKey.empty",
        twoRequiredKey = s"$accountingPeriodEndDateKey.two-required",
        requiredKey = s"$accountingPeriodEndDateKey.required",
        args = Seq(entityType)
      )
        .verifying(minDate(DST_EPOCH, s"$accountingPeriodEndDateKey.minimum-date", entityType))
        .verifying(maxAccountingPeriodEndDate(liabilityDate, entityType))
    )
  }

  private def maxAccountingPeriodEndDate(liabilityDate: LocalDate, args: Any*): Constraint[LocalDate] =
    Constraint {
      case ap if liabilityDate == DST_EPOCH && ap.isAfter(liabilityDate.plusYears(1).minusDays(1)) =>
        Invalid(s"$accountingPeriodEndDateKey.fixed-maximum-date", args: _*)
      case ap if ap.isAfter(liabilityDate.plusYears(1).minusDays(1))                               =>
        Invalid(s"$accountingPeriodEndDateKey.maximum-date", args: _*)
      case _                                                                                       => Valid
    }
}
