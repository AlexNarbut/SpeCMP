package ru.alarmtrade.specmp.feature.productinfo.domain.model

enum class ProductQueryError { EMPTY, NOT_DIGITS, TOO_LONG }

sealed interface ProductQuery {
    data class Valid(val id: Long) : ProductQuery

    data class Invalid(val error: ProductQueryError) : ProductQuery

    companion object {
        private val Separators = Regex("""[\s\-–—]""")

        /** Пробелы и дефисы из скопированного штрих-кода убираются; номер — только цифры и влезает в `Long`. */
        fun parse(raw: String): ProductQuery {
            val digits = raw.replace(Separators, "")
            return when {
                digits.isEmpty() -> Invalid(ProductQueryError.EMPTY)
                !digits.all { it in '0'..'9' } -> Invalid(ProductQueryError.NOT_DIGITS)
                else -> digits.toLongOrNull()?.let(::Valid) ?: Invalid(ProductQueryError.TOO_LONG)
            }
        }
    }
}
