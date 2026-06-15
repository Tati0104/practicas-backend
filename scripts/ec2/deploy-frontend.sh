#!/bin/bash
set -euo pipefail

SRC="${1:-${HOME}/frontend-dist}"
DEST="/var/www/practicas"

if [[ ! -f "${SRC}/index.html" ]]; then
  echo "No se encontró index.html en ${SRC}."
  echo "Sube el build con: scp -r frontend/dist/* ec2-user@HOST:~/frontend-dist/"
  exit 1
fi

sudo mkdir -p "$DEST"
sudo rm -rf "${DEST:?}"/*
sudo cp -r "${SRC}/." "$DEST/"
sudo chown -R nginx:nginx "$DEST"
sudo chmod -R a+rX "$DEST"

echo "Frontend desplegado en ${DEST}"
echo "Assets:"
sudo ls "$DEST/assets/" | head -5
