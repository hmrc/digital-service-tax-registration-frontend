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

import forms.behaviours.DateBehaviours
import models.DataValues.DST_EPOCH
import org.scalatest.{Assertion, EitherValues, OptionValues}
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class AccountingPeriodEndDateFormProviderSpec extends DateBehaviours with OptionValues with EitherValues {
  implicit val messages: Messages = stubMessages()

  val accountingPeriodEndDateKey                 = "accounting-period-end-date"
  private val givenLiabilityStartDate: LocalDate = LocalDate.of(2022, 6, 14)

  val form: Form[LocalDate] = new AccountingPeriodEndDateFormProvider()(true, givenLiabilityStartDate)

  "accounting-period-end-date field, must" - {
    "error" - {
      "when given" - {
        "empty values" - {
          behave like mandatoryField(
            form,
            accountingPeriodEndDateKey,
            apFormError("empty")
          )
        }

        "date earlier than the 1st April 2020" in {
          checkDateAgainst(
            form,
            LocalDate.of(2020, 3, 31),
            apFormError("minimum-date")
          )
        }

        "dates more than 1 year after the liability date" in {
          checkDateAgainst(
            form,
            givenLiabilityStartDate.plusYears(1).plusDays(2),
            apFormError("maximum-date")
          )
        }

        "dates more than 1 year after the liability date & the liability date is the 1st April 2020" in {
          val form: Form[LocalDate] = new AccountingPeriodEndDateFormProvider()(true, DST_EPOCH)
          checkDateAgainst(
            form,
            DST_EPOCH.plusYears(1).plusDays(2),
            apFormError("fixed-maximum-date")
          )
        }
      }
    }

    "allow" - {
      "dates" - {
        "between 1st April 2020 and within 1 year of the liability date" - {
          behave like dateField(form, accountingPeriodEndDateKey, genAccountingPeriodEndDate(givenLiabilityStartDate))
        }
      }
    }
  }

  private def apFormError(errorMsgSuffix: String): FormError =
    FormError(s"$accountingPeriodEndDateKey", s"$accountingPeriodEndDateKey.$errorMsgSuffix", Seq("group"))

  private def checkDateAgainst(form: Form[LocalDate], apEndDate: LocalDate, formError: FormError): Assertion = {
    val data = Map(
      s"$accountingPeriodEndDateKey.day"   -> apEndDate.getDayOfMonth.toString,
      s"$accountingPeriodEndDateKey.month" -> apEndDate.getMonthValue.toString,
      s"$accountingPeriodEndDateKey.year"  -> apEndDate.getYear.toString
    )

    val result = form.bind(data)

    result.errors must contain only formError
  }
}
