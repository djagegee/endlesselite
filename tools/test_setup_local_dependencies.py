from __future__ import annotations

import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).with_name("setup_local_dependencies.py")
SPEC = importlib.util.spec_from_file_location("endlesselite_setup_local_dependencies", MODULE_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"Cannot load {MODULE_PATH}")
setup = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = setup
SPEC.loader.exec_module(setup)


class LocalDependencySetupTest(unittest.TestCase):
    def test_manifest_covers_every_unredistributable_build_dependency(self) -> None:
        coordinates = {artifact.coordinates for artifact in setup.LOCAL_ARTIFACTS}
        self.assertEqual({
            "com.hypixel.hytale:hytale-server:0.5.0-local",
            "com.airijko:endless-leveling-core:11.6.1-local",
            "com.ziggfreed:mmo-skill-tree:1.5.2-local",
            "com.narwhals:perfect-utils:1.1.0-local",
            "com.airijko:endless-guilds:1.13.0-local",
        }, coordinates)

    def test_hash_validation_fails_closed_before_install(self) -> None:
        artifact = setup.LocalArtifact(
            Path("mods/example.jar"), "example", "artifact", "1-local", "0" * 64)
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / artifact.relative_path
            source.parent.mkdir(parents=True)
            source.write_bytes(b"unexpected")
            with self.assertRaisesRegex(RuntimeError, "Unexpected SHA-256"):
                setup.validated_source(root, artifact)

    def test_install_arguments_use_normal_maven_coordinates(self) -> None:
        artifact = setup.LocalArtifact(
            Path("HytaleServer.jar"), "com.hypixel.hytale", "hytale-server", "0.5.0-local", "a" * 64)
        source = Path("C:/server/HytaleServer.jar")
        self.assertEqual([
            "install:install-file",
            f"-Dfile={source}",
            "-DgroupId=com.hypixel.hytale",
            "-DartifactId=hytale-server",
            "-Dversion=0.5.0-local",
            "-Dpackaging=jar",
            "-DgeneratePom=true",
        ], setup.install_arguments(source, artifact))


if __name__ == "__main__":
    unittest.main()