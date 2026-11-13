package ru.alarmtrade.specmp.core.common.phone

/**
 * Телефон в международном формате. Значение — только цифры с кодом страны, без `+` (`79001234567`);
 * наружу (сервер) — [toE164]. Маски и длины — Европа и СНГ, прочие коды — группы по 3 и 8–15 цифр.
 */
object PhoneNumbers {
    const val MAX_DIGITS = 15

    private const val RU_TRUNK_LENGTH = 11
    private const val GENERIC_MIN = 8
    private const val GROUP = 3

    /** Вставка или значение извне: только цифры; `8XXXXXXXXXX` без `+` — российский формат, становится `7…`. */
    fun fromRaw(raw: String): String {
        val digits = raw.filter(Char::isDigit)
        val trunk = !raw.trim().startsWith('+') && digits.length == RU_TRUNK_LENGTH && digits.startsWith('8')
        val normalized = if (trunk) "7" + digits.drop(1) else digits
        return normalized.take(maxDigits(normalized))
    }

    /** Ввод в поле: только цифры, не длиннее, чем допускает распознанная страна. */
    fun fromInput(text: String): String {
        val digits = text.filter(Char::isDigit)
        return digits.take(maxDigits(digits))
    }

    fun maxDigits(digits: String): Int = country(digits)?.let { it.code.length + it.maxLength } ?: MAX_DIGITS

    fun toE164(digits: String): String = "+$digits"

    fun isValid(digits: String): Boolean {
        if (digits.isEmpty() || digits.any { !it.isDigit() } || digits.startsWith('0')) return false
        val country = country(digits) ?: return digits.length in GENERIC_MIN..MAX_DIGITS
        return digits.length - country.code.length in country.minLength..country.maxLength
    }

    /** `+7 (900) 123-45-67`; для неполного номера — без хвостовых разделителей. */
    fun format(digits: String): String {
        if (digits.isEmpty()) return "+"
        val country = country(digits) ?: return "+" + digits.chunked(GROUP).joinToString(" ")
        val national = digits.drop(country.code.length)
        val prefix = "+" + country.code
        if (national.isEmpty()) return prefix
        val mask = country.mask ?: return prefix + " " + national.chunked(GROUP).joinToString(" ")
        return prefix + applyMask(mask, national)
    }

    private fun applyMask(mask: String, national: String): String = buildString {
        var next = 0
        val pending = StringBuilder()
        for (char in mask) {
            if (next == national.length) break
            if (char == SLOT) {
                append(pending)
                pending.clear()
                append(national[next++])
            } else {
                pending.append(char)
            }
        }
        if (next < national.length) append(national.substring(next))
    }

    private fun country(digits: String): PhoneCountry? =
        PhoneCountries.all.filter { digits.startsWith(it.code) }.maxByOrNull { it.code.length }

    internal const val SLOT = 'X'
}

/** [mask] — национальная часть после кода (`X` — цифра); `null` — переменная длина, группы по 3. */
internal data class PhoneCountry(
    val code: String,
    val mask: String?,
    val minLength: Int,
    val maxLength: Int = minLength,
)

internal object PhoneCountries {
    val all: List<PhoneCountry> = listOf(
        PhoneCountry("7", " (XXX) XXX-XX-XX", 10),
        PhoneCountry("375", " (XX) XXX-XX-XX", 9),
        PhoneCountry("380", " (XX) XXX-XX-XX", 9),
        PhoneCountry("373", " XX XXX XXX", 8),
        PhoneCountry("374", " XX XXX-XXX", 8),
        PhoneCountry("994", " XX XXX-XX-XX", 9),
        PhoneCountry("995", " XXX XX-XX-XX", 9),
        PhoneCountry("996", " XXX XXX-XXX", 9),
        PhoneCountry("998", " XX XXX-XX-XX", 9),
        PhoneCountry("992", " XX XXX-XXXX", 9),
        PhoneCountry("993", " XX XX-XX-XX", 8),
        PhoneCountry("370", " XXX XXXXX", 8),
        PhoneCountry("371", " XX XXX XXX", 8),
        PhoneCountry("372", " XXXX XXXX", 7, 8),

        PhoneCountry("30", " XXX XXX XXXX", 10),
        PhoneCountry("31", " X XXXX XXXX", 9),
        PhoneCountry("33", " X XX XX XX XX", 9),
        PhoneCountry("34", " XXX XXX XXX", 9),
        PhoneCountry("40", " XXX XXX XXX", 9),
        PhoneCountry("41", " XX XXX XX XX", 9),
        PhoneCountry("45", " XX XX XX XX", 8),
        PhoneCountry("47", " XXX XX XXX", 8),
        PhoneCountry("48", " XXX XXX XXX", 9),
        PhoneCountry("90", " XXX XXX XX XX", 10),
        PhoneCountry("350", " XXXX XXXX", 8),
        PhoneCountry("351", " XXX XXX XXX", 9),
        PhoneCountry("354", " XXX XXXX", 7),
        PhoneCountry("356", " XXXX XXXX", 8),
        PhoneCountry("357", " XX XXXXXX", 8),
        PhoneCountry("376", " XXX XXX", 6),
        PhoneCountry("382", " XX XXX XXX", 8),
        PhoneCountry("386", " XX XXX XXX", 8),
        PhoneCountry("387", " XX XXX XXX", 8),
        PhoneCountry("389", " XX XXX XXX", 8),
        PhoneCountry("420", " XXX XXX XXX", 9),
        PhoneCountry("421", " XXX XXX XXX", 9),
        PhoneCountry("423", " XXX XX XX", 7),

        PhoneCountry("32", null, 8, 9),
        PhoneCountry("36", null, 8, 9),
        PhoneCountry("39", null, 6, 11),
        PhoneCountry("43", null, 7, 13),
        PhoneCountry("44", null, 9, 10),
        PhoneCountry("46", null, 7, 10),
        PhoneCountry("49", null, 6, 13),
        PhoneCountry("352", null, 6, 11),
        PhoneCountry("353", null, 7, 9),
        PhoneCountry("355", null, 8, 9),
        PhoneCountry("358", null, 5, 12),
        PhoneCountry("359", null, 8, 9),
        PhoneCountry("377", null, 8, 9),
        PhoneCountry("378", null, 6, 10),
        PhoneCountry("381", null, 8, 9),
        PhoneCountry("383", null, 8, 9),
        PhoneCountry("385", null, 8, 9),
    )
}
