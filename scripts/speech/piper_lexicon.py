#!/usr/bin/env python3
"""Словарь произношений для голоса Piper (core/speech) и эталон ID фонем для теста.

Запуск — вручную на машине разработчика (в CI не нужен), интерпретатором окружения с пакетом piper-tts:

    <venv>/bin/python scripts/speech/piper_lexicon.py [--config <ru_RU-dmitri-medium.onnx.json>]

Вход: строки values-ru из core/localization по KEY_PATTERNS, фразы scripts/speech/phrases.txt и исключения
scripts/speech/exceptions.tsv. Выход: LEXICON_OUT (словарь, грузит RussianG2p) и REFERENCE_OUT (эталон: ID фонем,
которые строит сам piper). Добавились строки озвучки — дописать ключи в KEY_PATTERNS или фразы в phrases.txt и
перезапустить; проверка «словарь даёт те же ID, что piper» печатается в конце.
"""

import argparse
import glob
import json
import os
import re
import sys
import unicodedata
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VALUES_RU = ROOT / "core/localization/src/commonMain/composeResources/values-ru"
PHRASES = ROOT / "scripts/speech/phrases.txt"
EXCEPTIONS = ROOT / "scripts/speech/exceptions.tsv"
LEXICON_OUT = ROOT / "core/speech/src/jvmAndAndroidMain/resources/piper/ru_lexicon.tsv"
REFERENCE_OUT = ROOT / "core/speech/src/jvmTest/resources/piper/reference.tsv"
VOICE_CONFIG = ROOT / "core/speech/src/jvmAndAndroidMain/resources/piper/ru_RU-dmitri-medium.onnx.json"

KEY_PATTERNS = [
    r"autostart_",
    r"devicestate_(zone|fast)_",
    r"fastmount_check_",
    r"fastmount_voice_",
]

# Слово, за которым espeak читает слово «как внутри фразы» (без оглушения на паузе и без ассимиляции).
NEUTRAL_NEXT = "ага"

WORD = re.compile(r"[0-9a-zа-яё]+(?:-[0-9a-zа-яё]+)*")
CLAUSE_BREAKS = {",": ",", ";": ";", ":": ":", "—": ";", "–": ";", ".": ".", "!": "!", "?": "?", "…": ""}
SENTENCE_END = {".", "!", "?"}
IGNORED = set("\"'«»()„“”")
CLOSING = set(")»\"”'")


def load_texts() -> list[str]:
    patterns = [re.compile(p) for p in KEY_PATTERNS]
    texts = []
    for path in sorted(glob.glob(str(VALUES_RU / "*.xml"))):
        for element in ET.parse(path).getroot().iter("string"):
            if any(p.match(element.get("name", "")) for p in patterns):
                texts.append(clean_resource("".join(element.itertext())))
    for line in PHRASES.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            texts.append(line)
    return list(dict.fromkeys(t for t in texts if t))


def clean_resource(text: str) -> str:
    text = re.sub(r"%(\d+\$)?[sd]", " ", text)
    return text.replace("\\n", " ").replace("\\'", "'").replace('\\"', '"').strip()


def load_exceptions() -> dict[str, str]:
    result = {}
    for line in EXCEPTIONS.read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        word, spelling = line.split("\t")
        result[word.strip().lower()] = spelling.strip()
    return result


def tokenize(text: str):
    """Фраза → клаузы [(слова, терминатор)] по тем же правилам, что RussianG2p; None — есть непроизносимое."""
    text = text.replace("...", "…")
    clauses, words, i = [], [], 0
    while i < len(text):
        match = WORD.match(text.lower(), i)
        if match:
            words.append(match.group())
            i = match.end()
            continue
        char = text[i]
        if char in CLAUSE_BREAKS:
            if breaks_clause(text, i) and words:
                clauses.append((words, CLAUSE_BREAKS[char]))
                words = []
        elif char not in IGNORED and char != "-" and not char.isspace():
            return None
        i += 1
    if words:
        clauses.append((words, ""))
    return clauses


def breaks_clause(text: str, index: int) -> bool:
    """Как espeak: знак рвёт клаузу, только если за ним (после закрывающих скобок и кавычек) пробел или конец текста;
    точка перед словом со строчной буквы — сокращение («доп. датчик»)."""
    i = index + 1
    while i < len(text) and text[i] in CLOSING:
        i += 1
    if i < len(text) and not text[i].isspace():
        return False
    if text[index] == ".":
        rest = text[i:].lstrip()
        if rest and rest[0].islower():
            return False
    return True


class Espeak:
    def __init__(self):
        import piper

        # espeak-ng падает на длинном пути к данным — работаем из каталога пакета с относительным путём.
        os.chdir(Path(piper.__file__).parent)
        from piper import espeakbridge
        from piper.phonemize_espeak import EspeakPhonemizer

        EspeakPhonemizer("espeak-ng-data")
        espeakbridge.set_voice("ru")
        self.bridge = espeakbridge

    def clauses(self, text: str):
        return [(re.sub(r"\([^)]+\)", "", p), t, eos) for p, t, eos in self.bridge.get_phonemes(text)]

    def words(self, text: str) -> list[str]:
        return [w for p, _, _ in self.clauses(text) for w in p.split()]

    def single(self, text: str):
        result = self.words(text)
        return result[0] if len(result) == 1 else None


def build_lexicon(texts, exceptions, espeak):
    def spelling(word):
        return exceptions.get(word, word)

    inner, final, pairs = {}, {}, defaultdict(Counter)
    unknown = set()
    for text in texts:
        clauses = tokenize(text)
        if clauses is None:
            continue
        words = [w for ws, _ in clauses for w in ws]
        for word in words:
            if word in inner or word in unknown:
                continue
            probe = espeak.words(f"{spelling(word)} {NEUTRAL_NEXT}")
            alone = espeak.single(spelling(word))
            if len(probe) != 2 or alone is None or "(" in "".join(espeak.bridge.get_phonemes(spelling(word))[0][0]):
                unknown.add(word)
                continue
            inner[word] = nfd(probe[0])
            final[word] = nfd(alone)
        if any(w in unknown for w in words):
            continue
        spoken = " ".join(" ".join(spelling(w) for w in ws) + term for ws, term in clauses)
        observed = espeak.words(spoken)
        if len(observed) != len(words):
            continue
        for index, word in enumerate(words):
            nxt = words[index + 1] if index + 1 < len(words) else "$"
            pairs[(word, nxt)][nfd(observed[index])] += 1
    return inner, final, pairs, unknown


def nfd(phonemes: str) -> str:
    """Как piper: фонемы раскладываются на кодовые точки NFD (ударения и диакритика — отдельные символы)."""
    return unicodedata.normalize("NFD", phonemes)


def lexicon_lines(inner, final, pairs):
    lines = []
    for word in sorted(inner):
        if final[word] != inner[word]:
            lines.append(f"{word}\t{inner[word]}\t{final[word]}")
        else:
            lines.append(f"{word}\t{inner[word]}")
    for (word, nxt), forms in sorted(pairs.items()):
        form = forms.most_common(1)[0][0]
        default = final[word] if nxt == "$" else inner[word]
        if nxt != "$" and form != default:
            lines.append(f"{word} {nxt}\t{form}")
    return lines


def g2p(text, lexicon_inner, lexicon_final, lexicon_pairs):
    """Повтор алгоритма RussianG2p (Kotlin): предложения → строки фонем."""
    clauses = tokenize(text)
    if clauses is None:
        return None
    sentences, current = [], ""
    for index, (words, term) in enumerate(clauses):
        parts = []
        for i, word in enumerate(words):
            if word not in lexicon_inner:
                return None
            nxt = words[i + 1] if i + 1 < len(words) else None
            if nxt is not None and (word, nxt) in lexicon_pairs:
                parts.append(lexicon_pairs[(word, nxt)])
            elif nxt is None:
                parts.append(lexicon_final.get(word, lexicon_inner[word]))
            else:
                parts.append(lexicon_inner[word])
        current += " ".join(parts) + term
        if term in (",", ":", ";"):
            current += " "
        if term in SENTENCE_END:
            sentences.append(current)
            current = ""
    if current:
        sentences.append(current)
    return sentences


def to_ids(phonemes: str, id_map, clusters) -> list[int]:
    phones = list(unicodedata.normalize("NFD", phonemes))
    longest = max((len(c) for c in clusters), default=1)
    merged, i = [], 0
    while i < len(phones):
        size = next((n for n in range(longest, 1, -1) if tuple(phones[i : i + n]) in clusters), 1)
        merged.append("".join(phones[i : i + size]))
        i += size
    ids = id_map["^"] + id_map["_"]
    for phone in merged:
        if phone in id_map:
            ids += id_map[phone] + id_map["_"]
    return ids + id_map["$"]


def piper_ids(voice, text):
    return [voice.phonemes_to_ids(s) for s in voice.phonemize(text) if s]


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--config", type=Path, help="ru_RU-dmitri-medium.onnx.json (по умолчанию — из ресурсов core/speech)")
    args = parser.parse_args()
    config_path = (args.config or VOICE_CONFIG).resolve()
    config = json.loads(config_path.read_text(encoding="utf-8"))
    id_map = config["phoneme_id_map"]
    clusters = {tuple(c) for c in config.get("vowel_clusters", [])}

    texts = load_texts()
    exceptions = load_exceptions()
    espeak = Espeak()
    inner, final, pairs, unknown = build_lexicon(texts, exceptions, espeak)
    lines = lexicon_lines(inner, final, pairs)

    pair_map = {}
    for line in lines:
        key, form = line.split("\t")[:2]
        if " " in key:
            pair_map[tuple(key.split(" "))] = form

    from piper.config import PiperConfig
    from piper.voice import PiperVoice

    # Эталон — фонемизация и ID самого piper; ONNX-модель для этого не нужна.
    class TextOnlyVoice(PiperVoice):
        def __init__(self, cfg):
            self.config = cfg
            self.espeak_data_dir = "espeak-ng-data"

    voice = TextOnlyVoice(PiperConfig.from_dict(config))

    reference, mismatched, fallback = [], [], 0
    for text in texts:
        sentences = g2p(text, inner, final, pair_map)
        if sentences is None:
            fallback += 1
            continue
        ours = [to_ids(s, id_map, clusters) for s in sentences]
        respelled = respell(text, exceptions)
        theirs = piper_ids(voice, respelled)
        if ours != theirs:
            mismatched.append((text, sentences, voice.phonemize(respelled)))
            continue
        reference.append(f"{text}\t{'|'.join(','.join(map(str, s)) for s in theirs)}")

    LEXICON_OUT.parent.mkdir(parents=True, exist_ok=True)
    LEXICON_OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    REFERENCE_OUT.parent.mkdir(parents=True, exist_ok=True)
    REFERENCE_OUT.write_text("\n".join(reference) + "\n", encoding="utf-8")

    print(f"фраз: {len(texts)}, в эталоне: {len(reference)}, системным голосом: {fallback}, расхождений: {len(mismatched)}")
    print(f"слов в словаре: {len(inner)}, пар-исключений по контексту: {len(pair_map)}, незнакомых: {len(unknown)}")
    if unknown:
        print("незнакомые (фраза уйдёт в системный голос): " + ", ".join(sorted(unknown)))
    for text, ours, theirs in mismatched:
        print(f"РАСХОЖДЕНИЕ: {text}\n  наш:   {ours}\n  piper: {[''.join(s) for s in theirs]}")
    return 1 if mismatched else 0


def respell(text: str, exceptions: dict[str, str]) -> str:
    def replace(match):
        return exceptions.get(match.group().lower(), match.group())

    return re.sub(r"[0-9A-Za-zА-Яа-яЁё]+(?:-[0-9A-Za-zА-Яа-яЁё]+)*", replace, text)


if __name__ == "__main__":
    sys.exit(main())
