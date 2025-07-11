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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class ContactPersonNameFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactPersonNameFormProvider()()

  private val maxLength: Int     = 35
  private val fieldRegex: String = """^[a-zA-Z'&-^]{1,35}$"""

  Seq("firstName", "lastName") foreach { fieldName =>
    s".$fieldName" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        stringsWithMaxLength(maxLength, Gen.alphaChar)
      )

      behave like fieldWithMaxLengthGeneratingFromRegex(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, s"contactPersonName.error.$fieldName.length", Seq(maxLength)),
        regex = fieldRegex
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"contactPersonName.error.$fieldName.required")
      )

      behave like fieldWithInValidData(
        form,
        fieldName,
        stringsWithMaxLength(maxLength).suchThat(!_.matches(fieldRegex)),
        FormError(fieldName, s"contactPersonName.error.$fieldName.invalid", Seq(fieldRegex))
      )
    }
  }
}
