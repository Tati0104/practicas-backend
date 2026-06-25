#!/bin/bash
set -euo pipefail

CONF_SRC="${1:-${HOME}/nginx-practicas.conf}"
CONF_DEST="/etc/nginx/conf.d/practicas.conf"

if [[ ! -f "$CONF_SRC" ]]; then
  echo "No se encontró ${CONF_SRC}"
  exit 1
fi

sudo cp "$CONF_SRC" "$CONF_DEST"
sudo nginx -t
sudo systemctl reload nginx
echo "Nginx actualizado: ${CONF_DEST}"
