#!/bin/bash
set -euo pipefail

ENV_FILE="${HOME}/practi.env"
JAR="${HOME}/app.jar"
LOG="${HOME}/app.log"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Falta ${ENV_FILE}. Copia practi.env.example y completa los valores."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

pkill -f "java -jar ${JAR}" 2>/dev/null || true
sleep 2

nohup java -jar "$JAR" > "$LOG" 2>&1 &
echo "Backend iniciado (PID $!). Log: ${LOG}"
