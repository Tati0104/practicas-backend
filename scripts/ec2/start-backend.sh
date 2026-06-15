#!/bin/bash
set -euo pipefail

ENV_FILE="${HOME}/practi.env"
JAR="${HOME}/app.jar"
LOG="${HOME}/app.log"
PID_FILE="${HOME}/app.pid"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Falta ${ENV_FILE}. Copia practi.env.example y completa los valores."
  exit 1
fi

if [[ ! -f "$JAR" ]]; then
  echo "Falta ${JAR}. Sube el JAR antes de iniciar."
  exit 1
fi

stop_backend() {
  local pid=""
  if [[ -f "$PID_FILE" ]]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  fi
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
    sleep 2
  fi
  # Solo procesos java del JAR de producción (evita matar la sesión SSH)
  pid="$(pgrep -f "^java -jar ${JAR}$" | head -n 1 || true)"
  if [[ -n "$pid" ]]; then
    kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
    sleep 2
  fi
  rm -f "$PID_FILE"
}

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

stop_backend
: > "$LOG"

nohup java -jar "$JAR" >> "$LOG" 2>&1 &
echo "$!" > "$PID_FILE"

echo "Backend iniciado (PID $(cat "$PID_FILE")). Log: ${LOG}"
