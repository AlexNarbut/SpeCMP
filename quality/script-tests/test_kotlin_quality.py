import json

from support import RepoTestCase

SCRIPT = "scripts/agent-hooks/kotlin-quality.sh"


class KotlinQualityTest(RepoTestCase):
    """Только ветки без Gradle: разбор аргументов и payload агентов, отбор файлов."""

    scripts = (SCRIPT,)

    def test_rejects_unknown_option_and_format(self):
        self.assertEqual(64, self.run_script(SCRIPT, "--bogus").returncode)
        self.assertEqual(64, self.run_script(SCRIPT, "--format", "vim", "a.kt").returncode)

    def test_plain_mode_skips_non_kotlin_and_missing_files(self):
        self.write("docs/a.md", "x")

        result = self.run_script(SCRIPT, "docs/a.md", "missing.kt")

        self.assertEqual(0, result.returncode)
        self.assertIn("нет .kt/.kts внутри проекта", result.stdout)

    def test_agent_payloads_without_kotlin_files_pass_silently(self):
        self.write("docs/a.md", "x")
        payloads = [
            {"tool_name": "Write", "tool_input": {"file_path": str(self.repo / "docs/a.md")}},
            {"hook_event_name": "AfterTool", "tool_input": {"absolute_path": str(self.repo / "docs/a.md")}},
            {"tool_name": "apply_patch", "cwd": str(self.repo),
             "tool_input": {"command": "*** Begin Patch\n*** Update File: docs/a.md\n*** End Patch"}},
            {"tool_name": "Write", "tool_input": {"file_path": "/elsewhere/Foo.kt"}},
            "not json",
        ]
        for payload in payloads:
            with self.subTest(payload=payload):
                stdin = payload if isinstance(payload, str) else json.dumps(payload)
                result = self.run_script(SCRIPT, stdin=stdin)
                self.assertEqual(0, result.returncode, result.stderr)
                self.assertEqual("", result.stdout)
