#!/usr/bin/env bash
# validate-selector.sh — Run a Smithy selector against model files
# Usage: ./validate-selector.sh <selector> [model_path] [extra_args...]
#   selector   — Smithy selector expression (required)
#   model_path — Path to model files/directory/glob (default: model/)
#   extra_args — Passed through to smithy select (e.g. --show type,file)

set -euo pipefail

SELECTOR="${1:?Usage: $0 <selector> [model_path] [extra_args...]}"
MODEL_PATH="${2:-model/}"
shift 2 2>/dev/null || shift 1 2>/dev/null || true

if ! command -v smithy &>/dev/null; then
  echo "ERROR: Smithy CLI not found."
  OS="$(uname -s)"
  case "$OS" in
    Darwin) echo "  brew tap smithy-lang/tap && brew install smithy-cli" ;;
    Linux)
      ARCH="$(uname -m)"
      [[ "$ARCH" == "aarch64" ]] && ZIP="smithy-cli-linux-aarch64" || ZIP="smithy-cli-linux-x86_64"
      echo "  curl -L https://github.com/smithy-lang/smithy/releases/latest/download/${ZIP}.zip -o /tmp/${ZIP}.zip"
      echo "  mkdir -p /tmp/smithy-install && unzip -qo /tmp/${ZIP}.zip -d /tmp/smithy-install"
      echo "  sudo /tmp/smithy-install/${ZIP}/install" ;;
    *) echo "  See: https://smithy.io/2.0/guides/smithy-cli/cli_installation.html" ;;
  esac
  exit 1
fi

[ ! -e "$MODEL_PATH" ] && echo "ERROR: Model path not found: $MODEL_PATH" && exit 1

echo "Selector: $SELECTOR"
echo "Model:    $MODEL_PATH"
echo "---"
smithy select --aut --selector "$SELECTOR" "$MODEL_PATH" "$@"
