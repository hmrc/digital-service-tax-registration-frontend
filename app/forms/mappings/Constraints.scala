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

package forms.mappings

import forms.mappings.Constraints.postcodeRegex
import org.apache.commons.validator.routines.EmailValidator

import java.time.LocalDate
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import scala.util.matching.Regex

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum) {
        Valid
      } else {
        Invalid(errorKey, minimum)
      }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum && input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, minimum, maximum)
      }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _                         =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _                            =>
        Invalid(errorKey, maximum)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _                             =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _                              =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _                   =>
        Invalid(errorKey)
    }

  protected def isNotEmpty(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.nonEmpty =>
        Valid
      case _                        =>
        Invalid(errorKey, value)
    }

  protected def postcode(emptyPostcodeErrorKey: String): Constraint[String] =
    Constraint { p =>
      if (p == null) {
        Invalid(ValidationError(emptyPostcodeErrorKey))
      } else if (p.trim.isEmpty) {
        Invalid(ValidationError(emptyPostcodeErrorKey))
      } else {
        postcodeRegex.r
          .findFirstMatchIn(p.trim.replaceAll("[ \\t]+", " "))
          .map(_ => Valid)
          .getOrElse(Invalid(ValidationError("error.invalid.postcode")))
      }
    }

  protected def emailAddress(errorKey: String): Constraint[String] =
    Constraint { ea =>
      if (EmailValidator.getInstance().isValid(ea)) {
        Valid
      } else {
        Invalid(errorKey)
      }
    }
}

object Constraints {
  val postcodeRegex: String = """^[a-zA-Z]{1,2}[0-9][0-9a-zA-Z]?\s?[0-9][a-zA-Z]{2}$"""
  object CompanyName {
    val companyNameRegex: Regex = """^[a-zA-Z0-9 '&.-]{1,105}$""".r
    val maxLength               = 105
  }

  object Address {
    val addressRegex = """^[A-Za-z0-9 \-,.&']{1,35}$"""
  }
}
