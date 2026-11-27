#!/usr/bin/env python3
"""One-time setup for a fresh clone.

The repository intentionally does not ship the Minecraft mappings, the Evernight jar or the
remapped variants of it (all of them are large and/or third-party). This script regenerates them
from what a normal Loom project already has on disk:

  1. find the layered mappings Loom cached for the target Minecraft version
  2. write tools/int2named.tiny and tools/named2int.tiny (tiny v2, correct column order)
  3. download evernight-<version>.jar from GitHub Releases (with mirror fallbacks)
  4. remap it to named mappings as libs/evernight-named.jar

Run it once after cloning, then `python tools/build.py`.
"""
import glob
import os
import re
import shutil
import subprocess
import sys
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GRADLE = os.path.expanduser("~/.gradle/caches")
MC_VERSION = "1.21.11"
EVERNIGHT_VERSION = "1.2.5"
RELEASE_URL = ("https://github.com/Evernight-Client/Evernight/releases/download/"
               "v{version}/evernight-{version}.jar")
MIRRORS = (
    "https://gh-proxy.com/{url}",
    "https://ghfast.top/{url}",
    "https://ghproxy.net/{url}",
)


def module(pattern):
    matches = [m for m in glob.glob(pattern) if "sources" not in m]
    if not matches:
        raise SystemExit("missing dependency in the Gradle cache: " + pattern)
    return matches[0]


def remapper_classpath():
    base = os.path.join(GRADLE, "modules-2/files-2.1")
    names = ("asm-9.10.1", "asm-tree-9.10.1", "asm-commons-9.10.1", "asm-util-9.10.1",
             "asm-analysis-9.10.1", "mapping-io-0.8.0", "lorenz-tiny-4.0.2", "tiny-remapper-0.12.3")
    jars = []
    for root, _dirs, files in os.walk(base):
        if "sources" in root:
            continue
        for name in files:
            if name.endswith(".jar") and name.startswith(names):
                jars.append(os.path.join(root, name).replace(os.sep, "/"))
    if not jars:
        raise SystemExit("tiny-remapper is not in the Gradle cache; run a Loom build once first")
    return ";".join(jars)


def find_mappings():
    pattern = os.path.join(GRADLE, "fabric-loom", MC_VERSION, "loom.mappings.*", "mappings.tiny")
    matches = sorted(glob.glob(pattern))
    if not matches:
        raise SystemExit(
            "Layered mappings for Minecraft %s not found.\n"
            "Open the project once with Gradle/Loom (or any Loom 1.21.11 project) so it caches them, "
            "then run this script again." % MC_VERSION)
    return matches[-1]


def find_minecraft_jar(flavour):
    pattern = os.path.join(GRADLE, "fabric-loom", "minecraftMaven", "net", "minecraft",
                           "minecraft-merged%s" % flavour, MC_VERSION + "-*", "*.jar")
    matches = [m for m in glob.glob(pattern) if not m.endswith(".backup")]
    if not matches:
        raise SystemExit("Minecraft %s jar (%s) not found in the Loom cache" % (MC_VERSION, flavour))
    return matches[0]


def write_mappings(path, header, rows, convert):
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("tiny\t2\t0\t%s\t%s\n" % header)
        for row in rows:
            handle.write(convert(row))


def main():
    tools = os.path.join(ROOT, "tools")
    libs = os.path.join(ROOT, "libs")
    os.makedirs(libs, exist_ok=True)

    print("[1/4] reading layered mappings")
    source = find_mappings()
    lines = open(source, encoding="utf-8").read().split("\n")
    official_to_intermediary = {}
    official_to_named = {}
    rows = []
    for line in lines[1:]:
        if not line:
            continue
        parts = line.split("\t")
        if parts[0] == "c":
            official_to_intermediary[parts[1]] = parts[2]
            official_to_named[parts[1]] = parts[3]
            rows.append(("c", parts[1], parts[2], parts[3]))
        elif parts[0] == "":
            rows.append((parts[1], parts[2], parts[4], parts[5]))

    def remap_types(descriptor, table):
        return re.sub(r"L([^;]+);", lambda m: "L" + table.get(m.group(1), m.group(1)) + ";", descriptor)

    print("[2/4] writing tools/int2named.tiny and tools/named2int.tiny")
    int2named = os.path.join(tools, "int2named.tiny")
    with open(int2named, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("tiny\t2\t0\tintermediary\tnamed\n")
        for row in rows:
            if row[0] == "c":
                handle.write("c\t%s\t%s\n" % (row[2], row[3]))
            else:
                handle.write("\t%s\t%s\t%s\t%s\n"
                             % (row[0], remap_types(row[1], official_to_intermediary), row[2], row[3]))

    named2int = os.path.join(tools, "named2int.tiny")
    with open(named2int, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("tiny\t2\t0\tnamed\tintermediary\n")
        for row in rows:
            if row[0] == "c":
                handle.write("c\t%s\t%s\n" % (row[3], row[2]))
            else:
                # descriptor in the *source* namespace, then source name, then target name
                handle.write("\t%s\t%s\t%s\t%s\n"
                             % (row[0], remap_types(row[1], official_to_named), row[3], row[2]))

    # Sanity check: descriptors must be written in the *source* namespace. If they still contain
    # official (obfuscated) names, tiny-remapper silently keeps the named member names and the jar
    # only fails at runtime with NoSuchMethodError.
    sample = open(named2int, encoding="utf-8").read()
    if "()Lnet/minecraft/client/Minecraft;" not in sample:
        raise SystemExit("generated named2int.tiny has wrong descriptors; refusing to continue")

    # Sanity check: descriptors must be written in the *source* namespace. If they still contain
    # official (obfuscated) names, tiny-remapper silently keeps the named member names and the jar
    # only fails at runtime with NoSuchMethodError.
    sample = open(named2int, encoding="utf-8").read()
    if "()Lnet/minecraft/client/Minecraft;" not in sample:
        raise SystemExit("generated named2int.tiny has wrong descriptors; refusing to continue")

    print("[3/4] downloading evernight-%s.jar" % EVERNIGHT_VERSION)
    target = os.path.join(libs, "evernight-%s.jar" % EVERNIGHT_VERSION)
    if os.path.exists(target) and os.path.getsize(target) > 1_000_000:
        print("      already present, skipping download")
    else:
        url = RELEASE_URL.format(version=EVERNIGHT_VERSION)
        candidates = [url] + [mirror.format(url=url) for mirror in MIRRORS]
        last_error = None
        for candidate in candidates:
            try:
                print("      " + candidate)
                with urllib.request.urlopen(candidate, timeout=60) as response:
                    data = response.read()
                if len(data) < 1_000_000:
                    raise IOError("suspiciously small download (%d bytes)" % len(data))
                with open(target, "wb") as handle:
                    handle.write(data)
                break
            except Exception as error:  # noqa: BLE001 - mirrors fail in many different ways
                last_error = error
                print("      failed: %s" % error)
        else:
            raise SystemExit("could not download the Evernight jar: %s" % last_error)

    print("[4/4] remapping evernight to named mappings")
    named = os.path.join(libs, "evernight-named.jar")
    result = subprocess.run(
        ["java", "-cp", remapper_classpath(), "net.fabricmc.tinyremapper.Main",
         target, named, int2named, "intermediary", "named",
         find_minecraft_jar("-intermediary")],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    if result.returncode != 0:
        print(result.stdout[-2000:])
        print(result.stderr[-2000:])
        raise SystemExit("remapping failed")

    print("done: libs/evernight-%s.jar + libs/evernight-named.jar" % EVERNIGHT_VERSION)
    print("next: python tools/build.py")


if __name__ == "__main__":
    sys.exit(main())
