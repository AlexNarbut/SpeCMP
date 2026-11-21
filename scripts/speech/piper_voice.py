#!/usr/bin/env python3
"""Голос Piper ru_RU-dmitri-medium для core/speech: скачать исходник и сжать модель в int8.

Запуск — вручную на машине разработчика (в сборке и CI не нужен; результат лежит в git), интерпретатором окружения
с закреплёнными пакетами (см. core/speech/AGENTS.md):

    python3 -m venv <venv> && <venv>/bin/pip install onnxruntime==1.32.0 onnx==1.23.2 numpy==2.5.5
    <venv>/bin/python scripts/speech/piper_voice.py [--work <каталог для fp32>]

Скачивает fp32-модель, её .onnx.json и MODEL_CARD из rhasspy/piper-voices по закреплённой ревизии, сверяет SHA-256,
квантует веса (quantize_dynamic, QUInt8) и кладёт результат в OUT под исходным именем модели. Новая ревизия —
поменять REVISION и SHA-256 в SOURCES (поле lfs.oid у .onnx в api/models/rhasspy/piper-voices/tree/<rev>/...).
"""

import argparse
import hashlib
import logging
import shutil
import sys
import tempfile
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "core/speech/src/jvmAndAndroidMain/resources/piper"
VOICE = "ru_RU-dmitri-medium"
REVISION = "e929968c3761f1c281875787685a745863db838d"
BASE_URL = f"https://huggingface.co/rhasspy/piper-voices/resolve/{REVISION}/ru/ru_RU/dmitri/medium"
SOURCES = {
    f"{VOICE}.onnx": "9ed9941bac7373f2ec1be36627e0e71a349a59ca5522d4363fd50701db05193a",
    f"{VOICE}.onnx.json": "c3741c9c1558fc38e902fd80d39155c32a7d019d7d17e3a43eac66b7bd3fbe09",
    "MODEL_CARD": "4da875aca6642bd96f0327705e874cbbcc44bc1790ee1d08b3fc0eb266523e5d",
}
ONNXRUNTIME_VERSION = "1.32.0"
# Квантование детерминировано: с теми же исходником и версиями пакетов — тот же файл, что в git.
MODEL_SHA256 = "ba8bc8fe0dec77ff9f5675ec8437a3c3d2d35180c376e02e1afd3f334620ba2f"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 16), b""):
            digest.update(chunk)
    return digest.hexdigest()


def fetch(name: str, expected: str, work: Path) -> Path:
    target = work / name
    if target.is_file() and sha256(target) == expected:
        return target
    partial = work / f"{name}.part"
    print(f"Скачиваю {BASE_URL}/{name}")
    with urllib.request.urlopen(f"{BASE_URL}/{name}") as response, partial.open("wb") as out:
        shutil.copyfileobj(response, out)
    actual = sha256(partial)
    if actual != expected:
        partial.unlink()
        sys.exit(f"SHA-256 {name} не совпал: ожидали {expected}, получили {actual}")
    partial.replace(target)
    return target


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--work", type=Path, help="каталог для исходников (по умолчанию — временный)")
    args = parser.parse_args()

    import onnxruntime
    from onnxruntime.quantization import QuantType, quantize_dynamic

    if onnxruntime.__version__ != ONNXRUNTIME_VERSION:
        sys.exit(f"Нужен onnxruntime {ONNXRUNTIME_VERSION} (результат зависит от версии), установлен {onnxruntime.__version__}")

    work = args.work or Path(tempfile.mkdtemp(prefix="piper-voice-"))
    work.mkdir(parents=True, exist_ok=True)
    fetched = {name: fetch(name, expected, work) for name, expected in SOURCES.items()}

    OUT.mkdir(parents=True, exist_ok=True)
    for name in (f"{VOICE}.onnx.json", "MODEL_CARD"):
        shutil.copyfile(fetched[name], OUT / name)
    logging.getLogger().setLevel(logging.ERROR)
    quantized = work / f"{VOICE}.int8.onnx"
    quantize_dynamic(fetched[f"{VOICE}.onnx"], quantized, weight_type=QuantType.QUInt8)
    model = OUT / f"{VOICE}.onnx"
    shutil.copyfile(quantized, model)
    actual = sha256(model)
    print(f"{model.relative_to(ROOT)}: {model.stat().st_size} байт, SHA-256 {actual}")
    if actual != MODEL_SHA256:
        print(f"SHA-256 отличается от {MODEL_SHA256} — обновите MODEL_SHA256 и core/speech/AGENTS.md")


if __name__ == "__main__":
    main()
