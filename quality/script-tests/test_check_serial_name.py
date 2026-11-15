from support import RepoTestCase, git

SCRIPT = "config/hooks/check-serial-name.sh"


class CheckSerialNameTest(RepoTestCase):
    scripts = (SCRIPT,)

    def test_reports_serializable_without_serial_name(self):
        self.write("a/Foo.kt", "@Serializable\ndata class Foo(val id: Int)\n")

        result = self.run_script(SCRIPT, "a/Foo.kt")

        self.assertEqual(1, result.returncode)
        self.assertIn("a/Foo.kt:1: @Serializable без @SerialName", result.stdout)

    def test_accepts_serial_name_before_or_after_serializable(self):
        self.write("a/Before.kt", '@SerialName("a.Before")\n@Serializable\ndata class Before(val id: Int)\n')
        self.write("a/After.kt", '@Serializable\n@Immutable\n@SerialName("a.After")\nclass After\n')

        self.assertEqual(0, self.run_script(SCRIPT, "a/Before.kt", "a/After.kt").returncode)

    def test_reports_when_other_annotations_follow_without_serial_name(self):
        self.write("a/Foo.kt", "@Serializable\n@Immutable\ndata class Foo(val id: Int)\n")

        result = self.run_script(SCRIPT, "a/Foo.kt")

        self.assertEqual(1, result.returncode)
        self.assertIn("a/Foo.kt:1:", result.stdout)

    def test_ignores_serializable_with_custom_serializer_and_plain_files(self):
        self.write("a/Foo.kt", "@Serializable(with = FooSerializer::class)\nclass Foo\n\nclass Bar\n")

        self.assertEqual(0, self.run_script(SCRIPT, "a/Foo.kt").returncode)

    def test_line_numbers_restart_per_file(self):
        self.write("a/Ok.kt", '@Serializable\n@SerialName("a.Ok")\nclass Ok\n')
        self.write("a/Bad.kt", "class X\n@Serializable\nclass Bad\n")

        result = self.run_script(SCRIPT, "a/Ok.kt", "a/Bad.kt")

        self.assertEqual(1, result.returncode)
        self.assertEqual(["a/Bad.kt:2: @Serializable без @SerialName"], result.stdout.splitlines())

    def test_without_arguments_checks_tracked_kotlin_files(self):
        self.write("a/Bad.kt", "@Serializable\nclass Bad\n")
        self.write("a/Untracked.kt", "@Serializable\nclass Untracked\n")
        git(self.repo, "add", "a/Bad.kt")

        result = self.run_script(SCRIPT)

        self.assertEqual(1, result.returncode)
        self.assertIn("a/Bad.kt", result.stdout)
        self.assertNotIn("Untracked", result.stdout)
