#!/usr/bin/env python
"""Fail when public repository prose contains likely German text.

Intentional runtime localization and byte-exact recovery snapshots are excluded.
Every non-path-exempt marker line must match an audited path, line number, and full-line SHA-256.
"""
from __future__ import annotations

import hashlib
import hmac
import re
import subprocess
import sys
from pathlib import Path, PurePosixPath

ROOT = Path(__file__).resolve().parents[1]
TEXT_SUFFIXES = {".md", ".java", ".json", ".yml", ".yaml", ".xml", ".py", ".properties", ".txt", ".lang", ".ui", ".bson"}
TEXT_FILENAMES = {".gitattributes", ".gitignore"}
GERMAN_WORDS = set(bytes.fromhex(
    "616267657363686c6f7373656e0a616273696368746c6963680a61627374616e640a616b7475656c6c0a616c730a617573676566c3bc6872"
    "740a626572656974730a62657374616e64656e0a626577757373740a626c656962740a626cc3b6636b650a64616d69740a646172660a6461"
    "730a64656d0a64656e0a6465720a6465730a64657574736368650a6469650a64757263680a65696e650a65696e656d0a65696e656e0a6569"
    "6e65720a65726c617562740a6665686c65720a66c3a46869670a66c3a46869676b6569740a66c3a46869676b656974656e0a66c3bc720a67"
    "6567656e0a6765676e65720a67657363686c6f7373656e0a67657370657272740a67657465737465740a68696e776569730a6973740a6a65"
    "64650a6a656465720a6a65646f63680a6b65696e650a6b6c65696e65720a6b6f6e66696775726174696f6e0a6c6f6b616c650a6d69740a6d"
    "7573730a6e6163680a6e696368740a6e6f63680a6e75720a6f6465720a7072c3bc66656e0a7363686164656e0a73656b756e64656e0a7369"
    "63686572756e670a73696e640a736f7769650a7374616e64617264776572740a737461747573626572696368740a756e67c3bc6c7469670a"
    "756e7465720a76657277656e6465740a766f6c6c7374c3a46e6469670a766f720a7761726e756e670a77657264656e0a77657274650a7765"
    "727465626572656963680a776972640a77757264650a77757264656e0a7a69656c0a7a69656c650a7a756d0a7a75720a7a7769736368656e"
    "0ac3b66666656e746c6963680ac3bc626572"
).decode("utf-8").splitlines())
WORD_RE = re.compile(bytes.fromhex(
    "5b412d5a612d7a5c75303063302d5c75303064365c75303064382d5c75303066365c75303066382d5c75303066665d2b"
).decode("ascii"), re.UNICODE)
UMLAUT_RE = re.compile(bytes.fromhex(
    "5b5c75303065345c75303066365c75303066635c75303064665c75303063345c75303064365c75303064635d7c5c5c753030283f3a65347c66367c66637c64667c63347c64367c646329"
).decode("ascii"), re.IGNORECASE)
INTENTIONAL_LINE_HASHES: dict[str, dict[int, str]] = {'modules/classes/hymann/src/main/java/de/shadow/hymann/HymannClaimCommand.java': {49: '47b00446769565d20ddaefddf5faad32a8e96e77ece751f9e12ea7a03a82f9fd',
                                                                                   54: '2939c94a54922ab189e29b5ea10ed8e059352a0eb0f8c924e30353f1b3e10042',
                                                                                   59: 'fb54a7a5a48fb3bcf5b021f34792003d284ebda7a840ee4065b42a8db64d69ce',
                                                                                   60: '6f055ed1f752bf1798bea01b8f81417f51edb98d314c162867157e1f93f072e3',
                                                                                   65: '4aea01d745414c19d44520424c265ab6e206f4b08fa99eeb1e863f56af8d835a',
                                                                                   69: 'c402665b664024eaafa1ea5d81a01f05de782f271406f1c87146d9d4a4f0442d'},
 'modules/classes/nachtweber/src/main/java/de/shadow/nachtweber/NachtweberMmoContract.java': {14: '551c77d35177c8630a042903385ec2db7ac654d107634fa72e3867134f169579',
                                                                                              16: 'ccfbd2720de2322f59a67b0de048f07d77621ddc6d35fc1f8f060adef590f892'},
 'modules/classes/nachtweber/src/main/java/de/shadow/nachtweber/NachtweberMmoInstaller.java': {127: 'e343a31ea1834a0052801ceb9307f79ff3faa72725604fa83d14b716d605aa46'},
 'modules/classes/nachtweber/src/test/java/de/shadow/nachtweber/NachtweberMmoInstallerTest.java': {96: '0002809e921a25a7fd34339f7c5b08b00eb0cf60c5ff623a13f4b43b345d1016',
                                                                                                   102: '73ff5f7fe0caba1687102a9723df234d0a3fb878a738cf5078f92224a0f93b84'},
 'modules/classes/seuchenweber/src/main/java/de/shadow/seuchenweber/SeuchenweberPresentationInstaller.java': {49: '4f299fddb063e73d090b37f684855892a5f50150c82e93421c1c2db14548ac00',
                                                                                                              51: 'baf5606c68dba46161173130660090c89324d8dedbd4bf3064b2bd1ffc743d08',
                                                                                                              52: '08debcf7051d8d20ce3238bbd1b2b9605ebcd9350e8a493ddf773f7c617bf861',
                                                                                                              53: '82247d99c8b8198deaf3e847bac803a662b87cee9a71fc32131ef77c40335e7a',
                                                                                                              54: '45afdb5e6571c4e5e47ec20bcfd829f85fab96f0c26124eeaea397818e9445ef',
                                                                                                              55: '2bee51c246d92ef8372a23e5070f3a32f2e76cae49f2d4ad1657bc377e7351fd',
                                                                                                              57: '0b40c2d72619e0df2c1886d3fde4fde4fc7190898a116d84d09b1ffdb8656664',
                                                                                                              59: '3de5084754636b1ad64bc530e980081f122ecb448e0f8366414c50ff2365f503',
                                                                                                              61: '91a1871a152db28e9b804a3100c206579583d51222232f33a84aad97a50c57a3',
                                                                                                              62: 'cf090b5eb7f362d8c49d4c47a3890334439d93588b0a854c14f4737e5c54867b',
                                                                                                              63: 'df928abd5083539f95e6264f679551e415e68d4591049a9d4e84f76c97824fbb',
                                                                                                              65: '9fdeee30b9f726c4afe07eeb2bac26e3bde1f8850f2991c63e7305ecbc1a57eb',
                                                                                                              67: '91860b2bd63c805d56abb0496c2f40a8bf22b2d0d7d2c40d012db8acda69ed9a'},
 'modules/classes/seuchenweber/src/test/java/de/shadow/seuchenweber/SeuchenweberClientLocalizationTest.java': {32: 'db421fadce169df2d0c10961a43059ab697c295b444367e087f742a642499ffe',
                                                                                                               48: '57fb44323e9a254acd30feabef31ef1544f3ee411a0dea3b2f9a811d5b2f7bf0'},
 'modules/classes/seuchenweber/src/test/java/de/shadow/seuchenweber/SeuchenweberPresentationInstallerTest.java': {30: '3b4b2dda8f4867279ac3cbbc0d2a3d43d9e751cb23c168bce3bcf6622ddb1006'},
 'modules/content/rift-mage-dungeon/src/test/java/de/shadow/riftmage/RiftMagePortalAssetTest.java': {64: '0af9b66cbdec4043e5d05e68b3f662c6499e57964351ae2beec89cd49c12d780'},
 'modules/content/rift-mage-dungeon/src/test/java/de/shadow/riftmage/RiftMageTechnodistrictContractTest.java': {129: '4cbab948a6a11bab01906b32d7a46ffca88990f6e5746a3cd46fc85045ee4c3d'},
 'modules/hub/endless-book/src/main/java/de/shadow/endlessbook/EndlessBookConfig.java': {29: '3e213610529dd189633a456bb062cbe258fe949f5a84bc87bd3433ab3b539b8c'},
 'modules/hub/endless-book/src/main/java/de/shadow/endlessbook/EndlessBookDetailsPage.java': {52: '6acaae0771f929528629652e1d9266fd11a32cca328d170e76f56e7de4742e7b',
                                                                                              53: 'f2c911466c0a377c5a1013a8e0faa1297009af1901c16ca8282b626dbc3d3b5f',
                                                                                              54: '5a597390bcc6d5841c36512784eb256fae1406130d5bec07e0bb61f07f4fb4d0',
                                                                                              55: '69efa6610bb04e4f7d41539ff11359f9da274da224afd2e16f8d640f0ab2adc0'},
 'modules/hub/endless-book/src/main/java/de/shadow/endlessbook/EndlessBookPage.java': {49: '6acaae0771f929528629652e1d9266fd11a32cca328d170e76f56e7de4742e7b',
                                                                                       50: 'f2c911466c0a377c5a1013a8e0faa1297009af1901c16ca8282b626dbc3d3b5f',
                                                                                       51: '5a597390bcc6d5841c36512784eb256fae1406130d5bec07e0bb61f07f4fb4d0',
                                                                                       52: '69efa6610bb04e4f7d41539ff11359f9da274da224afd2e16f8d640f0ab2adc0'},
 'modules/hub/endless-book/src/main/java/de/shadow/endlessbook/PersonalClaimsCommand.java': {28: '7160a0e7789434c7a0459f91ced742d9a7bb7dbfd8668d9874152c1b0943f623'},
 'modules/hub/endless-book/src/main/java/de/shadow/endlessbook/PersonalClaimsPage.java': {316: 'e402ef61ae68d91c30c41415dc5933faefe1af2eceff3a40686c7720e79194fb',
                                                                                          322: 'e99dcce5bd2a9c83c0c3f19d321eccb3b2953535e3347010523e6e2e3c693c6c',
                                                                                          352: 'a7c5548acec65f1182e30f16ea8cd3938f247011c3d7c72b5e1771de5f605fae',
                                                                                          364: '6ce3bceea2c5f004d9436cc337afca686aa4ade72e424ae6c7d8bbf8d250332c',
                                                                                          394: 'a201f0e20b3f8ff488940b5f07568bbef1efa8cbecb2c06ddcb2967b570c588a',
                                                                                          400: 'eb7f758525d16146e373ce39f2e88599a59c24a5bd033e20e9066ed600f676dd',
                                                                                          406: '4317666ef6d7b4b9c1bd634ee9a85a52276769629edff4022346e7f100887056',
                                                                                          412: '4f59d86e306bec5737a6057b070359fecea7dd89fd0f9aebe589c05c7ba323e6',
                                                                                          418: 'fa6f097578fe7deb709e6a2d17b7c93a1526aa60a0d58c0f9b1c471f4cc0e4dd',
                                                                                          424: 'b63898c94a820ade7747b81d7fd9a20dce49c5e7b7c1713b76deb07a72682631',
                                                                                          442: '6b6388c3daa33bdbfa539db6cef0aaa510326226560e69ba1555a5f32471110a',
                                                                                          448: '5e3e7beebe126532737b8ee439db4743cf757e9eb7369001f136962389320e30',
                                                                                          460: '16a9eba8b9c955f3898aa00cbc1165e8cab73a650c400e82465eec9dc36bd233',
                                                                                          466: '564a48404f70f622f08b5dd485b7068c4f70f399bd0dbdc429bf094b3a2e5a1a',
                                                                                          472: '1c0ab5ac401689146ca08f4a4e94d13c074d617840641431278990359dab99a2',
                                                                                          478: '1f83723d713a9edb2ba6897942670a17553a9d11892eea49e260f50c59f427c0',
                                                                                          484: '205c5f16aea3f162fc8cde60b56ac2cefa0a42ffb1b676b1ec47f4c858591566',
                                                                                          490: '2c2b357fab7bb8e976b5add35fdc2b98081bd5743c2e54b5005d33a1f8449f44',
                                                                                          496: 'dce624923589008036a9ee06baf1e5a9192bcfd1386c341d8c60d2a08d9fafcf',
                                                                                          502: 'e20ed4384cbddb22b80cd517188f71c511ee4a208c2875f844c0ba39e9106435',
                                                                                          508: '00d48442c254413cd7a7a90a6eb3064df7c6f0360945b1179af662f63ab58921',
                                                                                          514: 'd338459ab763b0d67fffe1fbe8afae4100ab7db2894e64bc78fcdef3bfeec035',
                                                                                          520: '3ad508a34992e55c2fa2fbaced09b13ba84cc0f8c6f36f02de8512410a137930'}}


def tracked_files() -> list[str]:
    result = subprocess.run(
        ["git", "ls-files", "-z"], cwd=ROOT, check=True, stdout=subprocess.PIPE
    )
    return [item.decode("utf-8") for item in result.stdout.split(b"\0") if item]


def is_path_exempt(relative: str) -> bool:
    parts = PurePosixPath(relative).parts
    if "de-DE" in parts:
        return True
    return any(parts[index:index + 3] == ("src", "main", "snapshot") for index in range(len(parts) - 2))


def read_index_text(relative: str) -> str:
    result = subprocess.run(
        ["git", "show", f":{relative}"], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )
    if result.returncode != 0:
        detail = result.stderr.decode("utf-8", errors="replace").strip()
        raise OSError(detail or "unable to read staged blob")
    return result.stdout.decode("utf-8")


def has_german_marker(line: str) -> bool:
    if UMLAUT_RE.search(line):
        return True
    return bool({word.lower() for word in WORD_RE.findall(line)} & GERMAN_WORDS)


def is_audited_locale_line(relative: str, line_number: int, line: str) -> bool:
    expected = INTENTIONAL_LINE_HASHES.get(relative, {}).get(line_number)
    if expected is None:
        return False
    actual = hashlib.sha256(line.encode("utf-8")).hexdigest()
    return hmac.compare_digest(expected, actual)


def main() -> int:
    findings: list[tuple[str, int, str]] = []
    read_errors: list[str] = []
    scanned = 0
    for relative in tracked_files():
        path = PurePosixPath(relative)
        if is_path_exempt(relative) or (
            path.suffix.lower() not in TEXT_SUFFIXES and path.name not in TEXT_FILENAMES
        ):
            continue
        try:
            text = read_index_text(relative)
        except (UnicodeDecodeError, OSError) as error:
            read_errors.append(f"{relative}: {error}")
            continue
        scanned += 1
        for line_number, line in enumerate(text.splitlines(), 1):
            if has_german_marker(line) and not is_audited_locale_line(relative, line_number, line):
                findings.append((relative, line_number, line.strip()))
    if findings or read_errors:
        print("ENDLESS_ELITE_ENGLISH_PUBLIC_CONTENT_FAIL", file=sys.stderr)
        print(f"scanned={scanned} findings={len(findings)} read_errors={len(read_errors)}", file=sys.stderr)
        for error in read_errors:
            print(f"read-error: {error}", file=sys.stderr)
        for relative, line_number, line in findings:
            print(f"{relative}:{line_number}: {line}", file=sys.stderr)
        return 1
    print(f"ENDLESS_ELITE_ENGLISH_PUBLIC_CONTENT_PASS scanned={scanned}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
