import importlib.util
import pathlib
import unittest

worker_path = pathlib.Path(__file__).parents[2] / "desktop-admin" / "src" / "main" / "resources" / "translation" / "argos_worker.py"
spec = importlib.util.spec_from_file_location("argos_worker", worker_path)
worker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(worker)


class TranslationWorkerTest(unittest.TestCase):
    def test_long_text_is_split_without_losing_characters(self):
        text = "  " + ("Consulta con  varios síntomas.\n" * 50) + ("á" * 350) + "\n "
        segments = worker.split_text(text)
        self.assertEqual(text, "".join(segments))
        self.assertTrue(all(len(segment.strip()) <= 100 for segment in segments))
        self.assertEqual(text, worker.translate_text(text, lambda value: value))

    def test_empty_segment_translation_is_an_error(self):
        with self.assertRaises(ValueError):
            worker.translate_text("Descripción", lambda _: "")


if __name__ == "__main__":
    unittest.main()
