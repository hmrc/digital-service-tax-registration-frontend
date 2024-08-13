package forms

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints.CompanyName.maxLength
import play.api.data.FormError

class CompanyNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyName.error.required"
  val lengthKey = "companyName.error.length"

  val form = new CompanyNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      genCompanyName
    )
  }
}
