package ru.alarmtrade.specmp.core.speech.piper

internal sealed interface Phonemized {
    data class Sentences(val sentences: List<String>) : Phonemized

    /** Слова нет в словаре (или знак, который не читаем) — вся фраза уходит системному голосу. */
    data class Unknown(val token: String) : Phonemized
}

/**
 * Русский G2P по словарю `scripts/speech/piper_lexicon.py`. Строки словаря: `слово<TAB>фонемы[<TAB>в конце
 * клаузы]` и `слово следующее<TAB>фонемы` (форма перед конкретным словом: озвончение, оглушение). Клаузы,
 * терминаторы и предложения — как у espeak-ng в piper; алгоритм повторяет `g2p()` скрипта.
 */
internal class RussianG2p private constructor(
    private val inner: Map<String, String>,
    private val final: Map<String, String>,
    private val beforeWord: Map<String, String>,
) {

    fun phonemize(text: String): Phonemized {
        val clauses = when (val tokens = tokenize(text)) {
            is Tokens.Clauses -> tokens.clauses
            is Tokens.Unknown -> return Phonemized.Unknown(tokens.token)
        }
        val sentences = mutableListOf<String>()
        val current = StringBuilder()
        for (clause in clauses) {
            clause.words.forEachIndexed { index, word ->
                val form = phonemes(word, clause.words.getOrNull(index + 1)) ?: return Phonemized.Unknown(word)
                if (index > 0) current.append(' ')
                current.append(form)
            }
            current.append(clause.terminator)
            if (clause.terminator in PAUSES) current.append(' ')
            if (clause.terminator in SENTENCE_ENDS) {
                sentences += current.toString()
                current.clear()
            }
        }
        if (current.isNotEmpty()) sentences += current.toString()
        return Phonemized.Sentences(sentences)
    }

    private fun phonemes(word: String, next: String?): String? {
        val base = inner[word] ?: return null
        return when (next) {
            null -> final[word] ?: base
            else -> beforeWord["$word $next"] ?: base
        }
    }

    private fun tokenize(source: String): Tokens {
        val text = source.replace("...", "…")
        val lower = text.lowercase()
        val clauses = mutableListOf<Clause>()
        val words = mutableListOf<String>()
        var index = 0
        while (index < text.length) {
            val word = WORD.matchAt(lower, index)
            if (word != null) {
                words += word.value
                index = word.range.last + 1
                continue
            }
            val char = text[index]
            val terminator = CLAUSE_BREAKS[char]
            when {
                terminator != null -> if (breaksClause(text, index) && words.isNotEmpty()) {
                    clauses += Clause(words.toList(), terminator)
                    words.clear()
                }

                char.isWhitespace() || char == '-' || char in IGNORED -> Unit

                else -> return Tokens.Unknown(char.toString())
            }
            index++
        }
        if (words.isNotEmpty()) clauses += Clause(words.toList(), "")
        return Tokens.Clauses(clauses)
    }

    // Как espeak: знак рвёт клаузу, только если за ним (после закрывающих скобок и кавычек) пробел или конец текста;
    // точка перед словом со строчной буквы — сокращение («доп. датчик»).
    private fun breaksClause(text: String, index: Int): Boolean {
        var next = index + 1
        while (next < text.length && text[next] in CLOSING) next++
        if (next < text.length && !text[next].isWhitespace()) return false
        if (text[index] == '.') {
            val following = text.substring(next).trimStart().firstOrNull()
            if (following != null && following.isLowerCase()) return false
        }
        return true
    }

    private class Clause(val words: List<String>, val terminator: String)

    private sealed interface Tokens {
        class Clauses(val clauses: List<Clause>) : Tokens

        class Unknown(val token: String) : Tokens
    }

    companion object {
        fun parse(lexicon: String): RussianG2p {
            val inner = HashMap<String, String>()
            val final = HashMap<String, String>()
            val beforeWord = HashMap<String, String>()
            lexicon.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                val columns = line.split('\t')
                val key = columns[0]
                if (' ' in key) {
                    beforeWord[key] = columns[1]
                } else {
                    inner[key] = columns[1]
                    columns.getOrNull(2)?.let { final[key] = it }
                }
            }
            return RussianG2p(inner, final, beforeWord)
        }

        private val WORD = Regex("[0-9a-zа-яё]+(?:-[0-9a-zа-яё]+)*")
        private val CLAUSE_BREAKS = mapOf(
            ',' to ",",
            ';' to ";",
            ':' to ":",
            '—' to ";",
            '–' to ";",
            '.' to ".",
            '!' to "!",
            '?' to "?",
            '…' to "",
        )
        private val PAUSES = setOf(",", ":", ";")
        private val SENTENCE_ENDS = setOf(".", "!", "?")
        private val IGNORED = setOf('"', '\'', '«', '»', '(', ')', '„', '“', '”')
        private val CLOSING = setOf(')', '»', '"', '”', '\'')
    }
}
