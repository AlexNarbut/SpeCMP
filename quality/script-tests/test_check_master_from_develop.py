from support import RepoTestCase, git

SCRIPT = "config/hooks/check-master-from-develop.sh"
ZERO = "0" * 40


class CheckMasterFromDevelopTest(RepoTestCase):
    scripts = (SCRIPT,)

    def setUp(self) -> None:
        super().setUp()
        self.base = self.commit("base")
        git(self.repo, "branch", "main")
        self.develop = self.commit("develop work")

    def push(self, branch: str, to: str, frm: str):
        env = {"PRE_COMMIT_REMOTE_BRANCH": f"refs/heads/{branch}", "PRE_COMMIT_TO_REF": to,
               "PRE_COMMIT_FROM_REF": frm}
        return self.run_script(SCRIPT, "push", env=env)

    def feature_commit(self) -> str:
        git(self.repo, "checkout", "-q", "-b", "feature/x", "develop")
        sha = self.commit("feature work")
        git(self.repo, "checkout", "-q", "develop")
        return sha

    def test_push_of_develop_commits_to_main_passes(self):
        self.assertEqual(0, self.push("main", self.develop, self.base).returncode)

    def test_push_of_foreign_commit_to_main_is_blocked(self):
        feature = self.feature_commit()

        result = self.push("main", feature, self.base)

        self.assertEqual(1, result.returncode)
        self.assertIn("не из develop", result.stderr)
        self.assertIn("feature work", result.stderr)

    def test_push_to_other_branches_and_deletion_are_ignored(self):
        feature = self.feature_commit()

        self.assertEqual(0, self.push("feature/x", feature, self.base).returncode)
        self.assertEqual(0, self.push("main", ZERO, self.base).returncode)

    def test_merge_commit_of_develop_into_master_passes_push(self):
        git(self.repo, "checkout", "-q", "main")
        git(self.repo, "merge", "-q", "--no-ff", "--no-verify", "-m", "release", "develop")
        merge = git(self.repo, "rev-parse", "HEAD")

        self.assertEqual(0, self.push("master", merge, self.base).returncode)

    def test_merge_commit_of_feature_into_main_is_blocked_on_push(self):
        feature = self.feature_commit()
        git(self.repo, "checkout", "-q", "main")
        git(self.repo, "merge", "-q", "--no-ff", "--no-verify", "-m", "bad", feature)
        merge = git(self.repo, "rev-parse", "HEAD")

        self.assertEqual(1, self.push("main", merge, self.base).returncode)

    def test_merge_stage_blocks_feature_into_main_and_allows_develop(self):
        feature = self.feature_commit()
        git(self.repo, "checkout", "-q", "main")

        git(self.repo, "merge", "-q", "--no-ff", "--no-commit", feature)
        blocked = self.run_script(SCRIPT, "merge")
        git(self.repo, "merge", "--abort")
        git(self.repo, "merge", "-q", "--no-ff", "--no-commit", "develop")
        allowed = self.run_script(SCRIPT, "merge")

        self.assertEqual(1, blocked.returncode)
        self.assertEqual(0, allowed.returncode, allowed.stderr)

    def test_merge_stage_on_other_branch_is_ignored(self):
        feature = self.feature_commit()
        git(self.repo, "merge", "-q", "--no-ff", "--no-commit", feature)

        self.assertEqual(0, self.run_script(SCRIPT, "merge").returncode)

    def test_unknown_mode_is_usage_error(self):
        self.assertEqual(2, self.run_script(SCRIPT, "rebase").returncode)
