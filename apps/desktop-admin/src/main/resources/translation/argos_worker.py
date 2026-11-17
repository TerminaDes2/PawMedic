import json
import os
import re
import sys

os.environ["ARGOS_MODEL_PROVIDER"] = "OPENNMT"
os.environ["ARGOS_DEVICE_TYPE"] = "cpu"
os.environ["ARGOS_DEBUG"] = "0"
os.environ["HF_HUB_OFFLINE"] = "1"
os.environ["TRANSFORMERS_OFFLINE"] = "1"


def split_text(text, limit=100):
    """Keep all original characters while bounding translatable segments."""
    segments = []
    current = ""
    for part in re.split(r"(\s+)", text):
        if not part:
            continue
        if part.isspace():
            current += part
            continue
        for start in range(0, len(part), limit):
            word_part = part[start:start + limit]
            if len(current.lstrip()) + len(word_part) > limit and current.strip():
                segments.append(current)
                current = ""
            current += word_part
    if current:
        segments.append(current)
    return segments


def translate_text(text, translate):
    result = []
    for segment in split_text(text):
        content = segment.strip()
        if not content:
            result.append(segment)
            continue
        translated = translate(content)
        if not translated.strip():
            raise ValueError("Empty segment translation")
        leading = segment[:len(segment) - len(segment.lstrip())]
        trailing = segment[len(segment.rstrip()):]
        result.append(leading + translated + trailing)
    return "".join(result)


def main():
    sys.stdin.reconfigure(encoding="utf-8")
    sys.stdout.reconfigure(encoding="utf-8")
    import ctranslate2
    import argostranslate.package
    import argostranslate.translate

    request = json.load(sys.stdin)
    if request["source"] != "es" or request["target"] != "en":
        raise ValueError("Unsupported translation direction")
    texts = request.get("texts")
    if texts is None:
        texts = [request["text"]]
    if not isinstance(texts, list) or not texts or len(texts) > 50:
        raise ValueError("Invalid text list")
    if any(not isinstance(text, str) or not text.strip() or len(text) > 4000 for text in texts):
        raise ValueError("Invalid text")

    packages = [
        package for package in argostranslate.package.get_installed_packages()
        if package.type == "translate" and package.from_code == "es" and package.to_code == "en"
    ]
    if len(packages) != 1:
        raise ValueError("Expected one Spanish-to-English model")
    package = packages[0]
    model = ctranslate2.Translator(str(package.package_path / "model"), device="cpu")

    class BoundedSegment:
        def split_sentences(self, value):
            return [value]

    # Argos' built-in segmenters can download resources; the bounded segments
    # above are translated directly so this worker never invokes those paths.
    def translate(segment):
        return argostranslate.translate.apply_packaged_translation(
            package, segment, model, BoundedSegment(), num_hypotheses=1
        )[0].value

    translated = [translate_text(text, translate) for text in texts]
    response = {"translations": translated} if "texts" in request else {"translation": translated[0]}
    json.dump(response, sys.stdout, ensure_ascii=False)
    sys.stdout.flush()


if __name__ == "__main__":
    try:
        main()
    except Exception:
        sys.stderr.write("Local translation failed\n")
        sys.exit(1)
