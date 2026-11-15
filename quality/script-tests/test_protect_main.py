import json

from support import RepoTestCase, git

SCRIPT = "scripts/agent-hooks/protect-main.sh"


class ProtectMainTest(RepoTestCase):
    scripts = (SCRIPT,)

    def setUp(self) -> None:
        super().setUp()
        self.commit("base")
        git(self.repo, "branch", "main")

    def hook(self, command: str) -> int:
        payload = json.dumps({"tool_name": "Bash", "tool_input": {"command": command}})
        return self.run_script(SCRIPT, stdin=payload, env={"CLAUDE_PROJECT_DIR": str(self.repo)}).returncode

    def test_blocks_push_to_main_or_master(self):
        for command in ("git push origin main", "git push origin develop:master", "git -C . push -f origin main",
                        "git push origin HEAD:refs/heads/main", "git push origin main; echo ok"):
            with self.subTest(command=command):
                self.assertEqual(2, self.hook(command))

    def test_allows_push_to_develop_and_main_in_commit_message(self):
        for command in ("git push origin develop", 'git commit -m "fix main menu" && git push origin develop',
                        "git log main..develop", "ls"):
            with self.subTest(command=command):
                self.assertEqual(0, self.hook(command))

    def test_blocks_checkout_main_followed_by_merge(self):
        self.assertEqual(2, self.hook("git checkout main && git merge develop"))
        self.assertEqual(2, self.hook("git switch master; git pull"))

    def test_blocks_history_changes_while_on_main(self):
        git(self.repo, "checkout", "-q", "main")

        for command in ("git commit -m x", "git merge develop", "git tag v1", "git push", "git reset --hard"):
            with self.subTest(command=command):
                self.assertEqual(2, self.hook(command))
        self.assertEqual(0, self.hook("git status"))

    def test_ignores_empty_or_invalid_payload(self):
        self.assertEqual(0, self.run_script(SCRIPT, stdin="not json").returncode)
        self.assertEqual(0, self.run_script(SCRIPT, stdin="{}").returncode)
