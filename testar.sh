#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
bash compilar.sh
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.io.tmpdir=.tmp -cp out TestesAlgoritmos
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.io.tmpdir=.tmp -cp out TestesCurriculares
