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

  # Cualquier instancia del JAR (./app.jar o ruta absoluta)
  while read -r pid; do
    [[ -n "$pid" ]] && kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
  done < <(pgrep -f '[j]ava -jar.*app\.jar' || true)

  # Liberar puerto 8080 si quedó colgado
  if command -v lsof >/dev/null 2>&1; then
    while read -r pid; do
      [[ -n "$pid" ]] && kill -9 "$pid" 2>/dev/null || true
    done < <(lsof -t -i:8080 2>/dev/null || true)
  fi

  sleep 2
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
