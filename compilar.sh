#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out dist .tmp
javac -Xlint:all -encoding UTF-8 --release 17 -d out src/*.java
jar --create --file dist/planejamento-disciplinas.jar --main-class Main -C out .
