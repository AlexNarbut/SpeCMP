from support import RepoTestCase, git

SCRIPT = "config/hooks/check-strings.py"
RES = "core/localization/src/commonMain/composeResources"


def resources(*entries: str) -> str:
    return "<resources>\n" + "\n".join(f"    {e}" for e in entries) + "\n</resources>\n"


class CheckStringsTest(RepoTestCase):
    scripts = (SCRIPT,)

    def setUp(self) -> None:
        super().setUp()
        self.strings("values", 'login_form_title_main">Sign in')
        self.strings("values-ru", 'login_form_title_main">Вход')

    def strings(self, values: str, *names: str, file: str = "login.xml") -> None:
        self.write(f"{RES}/{values}/{file}", resources(*(f'<string name="{n}</string>' for n in names)))
        git(self.repo, "add", "-A")

    def check(self):
        return self.run_script(SCRIPT)

    def test_passes_on_consistent_resources(self):
        result = self.check()

        self.assertEqual(0, result.returncode, result.stdout)

    def test_reports_key_with_prefix_other_than_file_name(self):
        self.strings("values", 'login_form_title_main">Sign in', 'auth_form_title_main">Auth')
        self.strings("values-ru", 'login_form_title_main">Вход', 'auth_form_title_main">Авт')

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn("auth_form_title_main — префикс должен быть «login_»", result.stdout)

    def test_reports_key_without_known_type(self):
        self.strings("values", 'login_form_caption">Sign in')
        self.strings("values-ru", 'login_form_caption">Вход')

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn("login_form_caption — формат", result.stdout)

    def test_common_file_allows_key_without_screen(self):
        self.strings("values", 'common_btn_ok">OK', file="common.xml")
        self.strings("values-ru", 'common_btn_ok">ОК', file="common.xml")

        self.assertEqual(0, self.check().returncode)

    def test_reports_missing_and_extra_keys_in_translation(self):
        self.strings("values-ru", 'login_form_btn_extra">Ещё')

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn(f"{RES}/values-ru/login.xml: нет string login_form_title_main", result.stdout)
        self.assertIn(f"{RES}/values-ru/login.xml: лишний string login_form_btn_extra", result.stdout)

    def test_untranslatable_keys_are_not_compared(self):
        self.write(f"{RES}/values/login.xml", resources(
            '<string name="login_form_title_main">Sign in</string>',
            '<string name="login_form_value_brand" translatable="false">Pandora</string>',
        ))
        git(self.repo, "add", "-A")

        self.assertEqual(0, self.check().returncode)

    def test_reports_missing_translation_file(self):
        self.strings("values", 'chat_list_title_main">Chat', file="chat.xml")

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn(f"{RES}/values-ru/chat.xml: нет файла", result.stdout)

    def test_reports_strings_outside_localization_module(self):
        self.write("feature/login/src/commonMain/composeResources/values/strings.xml", resources())
        git(self.repo, "add", "-A")

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn("feature/login/src/commonMain/composeResources/values/strings.xml: строки — только в", result.stdout)

    def test_reports_escaped_quotes(self):
        self.strings("values", "login_form_title_main\">Don\\'t")

        result = self.check()

        self.assertEqual(1, result.returncode)
        self.assertIn(f"{RES}/values/login.xml:2:", result.stdout)
