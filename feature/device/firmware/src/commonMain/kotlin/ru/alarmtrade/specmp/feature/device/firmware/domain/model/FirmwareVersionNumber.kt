package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * Номер версии прошивки для сравнения: `3.57`, `VER3.57`, `3.57 A`, `3.61-beta.2`, `3.63-alpha.5`.
 *
 * Числа сравниваются числами (`3.9 < 3.10`), в отличие от строк: старое приложение сравнивало строки.
 * Суффикс бывает двух видов:
 * - **пред-релиз** (`alpha`, `beta`, `rc`…): старше такой же версии без суффикса (`3.62-beta.2` < `3.62`);
 * - **ревизия** (любая другая буква, как на сервере: `3.57 A`, `3.57 B`, `3.57 C`): новее версии без буквы и
 *   упорядочена по букве (`3.57` < `3.57 A` < `3.57 B`).
 * Блок сообщает версию без буквы ревизии (`VER3.57`), поэтому «новее ли версия установленной» решает [compareCore] —
 * без ревизий; полное упорядочение ([compareTo]) нужно списку (архиву). Строка без чисел сравнивается как текст
 * (известный запасной вариант: формат версий сервера уточняется).
 */
class FirmwareVersionNumber private constructor(
    private val parts: List<Int>,
    private val label: String?,
    private val labelNumber: Int,
    private val text: String
) : Comparable<FirmwareVersionNumber> {

    override fun compareTo(other: FirmwareVersionNumber): Int = compare(other, withRevisions = true)

    /** Сравнение без букв-ревизий: `3.57 C` и `3.57` — одна и та же версия; пред-релизы по-прежнему старше. */
    fun compareCore(other: FirmwareVersionNumber): Int = compare(other, withRevisions = false)

    private fun compare(other: FirmwareVersionNumber, withRevisions: Boolean): Int {
        if (parts.isEmpty() && other.parts.isEmpty()) return text.compareTo(other.text)
        val size = maxOf(parts.size, other.parts.size)
        for (index in 0 until size) {
            val byPart = parts.getOrElse(index) { 0 }.compareTo(other.parts.getOrElse(index) { 0 })
            if (byPart != 0) return byPart
        }
        val byRank = rank(withRevisions).compareTo(other.rank(withRevisions))
        if (byRank != 0) return byRank
        // Одинаковый ранг: метки сравниваются, только если они учитываются (пред-релиз или ревизия при полном
        // сравнении).
        val labelsCount = rank(withRevisions) != 0
        return if (labelsCount && label != null && other.label != null) {
            label.compareTo(other.label).takeIf { it != 0 } ?: labelNumber.compareTo(other.labelNumber)
        } else {
            0
        }
    }

    // Пред-релиз < без суффикса < ревизия.
    private fun rank(withRevisions: Boolean): Int = when {
        label == null -> 0
        label in PRERELEASE_LABELS -> -1
        withRevisions -> 1
        else -> 0
    }

    override fun equals(other: Any?): Boolean = other is FirmwareVersionNumber && compareTo(other) == 0

    override fun hashCode(): Int = parts.dropLastWhile { it == 0 }.hashCode() * HASH_FACTOR + (label?.hashCode() ?: 0)

    override fun toString(): String = text

    companion object {
        private const val HASH_FACTOR = 31
        private val PRERELEASE_LABELS = setOf("alpha", "beta", "rc", "pre", "dev", "snapshot")
        private val pattern = Regex("""^(\d+(?:\.\d+)*)(?:[\s._-]*([A-Za-z]+)[\s._-]*(\d+)?)?""")

        fun parse(raw: String): FirmwareVersionNumber {
            val text = raw.trim().removePrefix("VER").trim()
            val match = pattern.find(text)
                ?: return FirmwareVersionNumber(emptyList(), null, 0, text)
            val (numbers, label, labelNumber) = match.destructured
            return FirmwareVersionNumber(
                parts = numbers.split('.').map { it.toIntOrNull() ?: 0 },
                label = label.lowercase().ifEmpty { null },
                labelNumber = labelNumber.toIntOrNull() ?: 0,
                text = text
            )
        }
    }
}

/** Отношение версии к установленной на блоке. */
enum class VersionRelation { Newer, Same, Older }

/** Отношение этой версии к [installed] (строки как их отдают блок и сервер); буквы-ревизии не учитываются. */
fun String.relationTo(installed: String): VersionRelation {
    val comparison = FirmwareVersionNumber.parse(this).compareCore(FirmwareVersionNumber.parse(installed))
    return when {
        comparison > 0 -> VersionRelation.Newer
        comparison < 0 -> VersionRelation.Older
        else -> VersionRelation.Same
    }
}
