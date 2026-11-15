import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def git(repo: Path, *args: str) -> str:
    env = {**os.environ, "GIT_AUTHOR_NAME": "t", "GIT_AUTHOR_EMAIL": "t@t", "GIT_COMMITTER_NAME": "t",
           "GIT_COMMITTER_EMAIL": "t@t", "GIT_CONFIG_GLOBAL": os.devnull, "GIT_CONFIG_NOSYSTEM": "1"}
    return subprocess.run(["git", *args], cwd=repo, env=env, check=True, capture_output=True, text=True).stdout.strip()


class RepoTestCase(unittest.TestCase):
    """Временный git-репозиторий с копиями проверяемых скриптов по тем же относительным путям."""

    scripts: tuple[str, ...] = ()

    def setUp(self) -> None:
        self.repo = Path(tempfile.mkdtemp(prefix="gate-test-"))
        self.addCleanup(shutil.rmtree, self.repo, ignore_errors=True)
        git(self.repo, "init", "-q", "-b", "develop")
        for script in self.scripts:
            target = self.repo / script
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(ROOT / script, target)

    def write(self, rel: str, text: str) -> Path:
        path = self.repo / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        return path

    def commit(self, message: str) -> str:
        self.write(f"{message.replace(' ', '_')}.txt", message)
        git(self.repo, "add", "-A")
        git(self.repo, "commit", "-q", "-m", message)
        return git(self.repo, "rev-parse", "HEAD")

    def run_script(self, script: str, *args: str, stdin: str = "", env: dict | None = None):
        return subprocess.run([str(self.repo / script), *args], cwd=self.repo, input=stdin, text=True,
                              capture_output=True, env={**os.environ, **(env or {})})
