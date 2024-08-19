package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UltimateParentCompanyInternationalAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new UltimateParentCompanyInternationalAddressFormProvider()()

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "ultimateParentCompanyInternationalAddress.error.line1.required"
    val lengthKey = "ultimateParentCompanyInternationalAddress.error.line1.length"
    val maxLength = 35

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
  }

  ".line2" - {

    val fieldName = "line2"
    val requiredKey = "ultimateParentCompanyInternationalAddress.error.line2.required"
    val lengthKey = "ultimateParentCompanyInternationalAddress.error.line2.length"
    val maxLength = 5

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
  }
}
