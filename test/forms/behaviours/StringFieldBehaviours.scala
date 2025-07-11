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

package forms.behaviours

import org.scalacheck.Gen
import play.api.data.{Form, FormError}

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") { (string: String) =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }

  def fieldWithMaxLengthGeneratingFromRegex(
    form: Form[_],
    fieldName: String,
    maxLength: Int,
    regex: String,
    lengthError: FormError
  ): Unit =
    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThanGivenRegex(maxLength, regex) -> "longString") { (string: String) =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }

  def fieldWithInValidData(
    form: Form[_],
    fieldName: String,
    invalidDataGenerator: Gen[String],
    invalidDataError: FormError
  ): Unit =
    s"not bind invalid characters" in {
      forAll(invalidDataGenerator -> "inValidDataItem") { dataItem: String =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.errors must contain only invalidDataError
      }
    }

  def fieldWithRegexpWithGenerator(
    form: Form[_],
    fieldName: String,
    regexp: String,
    generator: Gen[String],
    error: FormError
  ): Unit =
    s"not bind strings which do not match $regexp" in {
      forAll(generator) { string =>
        whenever(!string.matches(regexp) && string.nonEmpty) {
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(error)
        }
      }
    }
}
