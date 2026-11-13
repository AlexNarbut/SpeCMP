package ru.alarmtrade.specmp.core.barcode

/** Разрешение на камеру для сканера: [DENIED] — можно спросить снова, [BLOCKED] — только в настройках. */
enum class BarcodeScannerAccess { REQUESTING, GRANTED, DENIED, BLOCKED }

private val Separators = Regex("""[\s\-]""")

/** Текст кода без пробелов и дефисов — только цифры: они; иначе (буквы, ссылка в QR) `null`. */
fun digitsOf(text: String): String? = text.trim().replace(Separators, "").takeIf { digits ->
    digits.isNotEmpty() && digits.all { it in '0'..'9' }
}
