package cl.duoc.electromechanics.model

import cl.duoc.electromechanics.domain.validation.ValidationResult

data class FieldState(
    val value: String = "",
    val error: String? = null
) {
    val hasError: Boolean get() = error != null

    fun withValue(newValue: String, revalidate: (String) -> ValidationResult): FieldState =
        if (error != null) copy(value = newValue, error = revalidate(newValue).errorMessage)
        else copy(value = newValue)

    fun validated(validate: (String) -> ValidationResult): FieldState =
        copy(error = validate(value).errorMessage)
}
