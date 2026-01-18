#!/usr/bin/env bash
set -euo pipefail

# =============================================
#       qa (quick-app) Installer
# =============================================

PROG='qa'
REPO='Solidify-PL/quick-app'

# Colors (optional)
RED='\033[1;31m'
GREEN='\033[1;32m'
BLUE='\033[1;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $*" >&2; }
warn()    { echo -e "${RED}[WARN]${NC} $*" >&2; }
success() { echo -e "${GREEN}[OK]${NC} $*"; }
fatal()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# ---------------------------------------------
#      Configuration via environment variables
# ---------------------------------------------
: "${INSTALL_DIR:=/usr/local/bin}"
: "${VERSION:=latest}"
: "${OS:=}"
: "${ARCH:=}"

# ---------------------------------------------
#      System and architecture detection
# ---------------------------------------------
if [ -z "$OS" ]; then
    case "$(uname -s)" in
        Linux*)  OS=linux ;;
        Darwin*) OS=macos ;;
        *)       fatal "Unsupported system: $(uname -s)" ;;
    esac
fi

if [ -z "$ARCH" ]; then
    case "$(uname -m)" in
        x86_64|amd64)   ARCH=amd64 ;;
        aarch64|arm64)  ARCH=arm64 ;;
        *)              fatal "Unsupported architecture: $(uname -m)" ;;
    esac
fi

# ---------------------------------------------
#      Detecting latest version (portable method)
# ---------------------------------------------
if [ "$VERSION" = "latest" ] || [ -z "$VERSION" ]; then
    info "Detecting latest version..."

    # Fetch the page and extract the first tag after /releases/tag/
    page_content=$(curl -sL "https://github.com/${REPO}/releases")

    # Portable method - search for lines with /releases/tag/v and take the first one
    LATEST_TAG=$(echo "$page_content" \
        | grep -o '/releases/tag/v[0-9][^"]*' \
        | head -n 1 \
        | sed 's|/releases/tag/||')

    if [ -z "$LATEST_TAG" ]; then
        # Alternative method - sometimes GitHub changes HTML structure
        LATEST_TAG=$(echo "$page_content" \
            | grep -o 'v[0-9]\+\.[0-9]\+\.[0-9]\+' \
            | head -n 1)
    fi

    [ -z "$LATEST_TAG" ] && fatal "Failed to detect latest version. Check manually: https://github.com/${REPO}/releases"

    VERSION="$LATEST_TAG"
    info "Found latest version: ${VERSION}"
fi

# ---------------------------------------------
#      Preparing filename and URL
# ---------------------------------------------
RELEASE_URL="https://github.com/${REPO}/releases/download/${VERSION}"

# First .tar.gz, then .zip
for ext in "tar.gz" "zip"; do
    ARCHIVE_NAME="qa-${VERSION}-${OS}-${ARCH}.${ext}"
    ARCHIVE_URL="${RELEASE_URL}/${ARCHIVE_NAME}"

    info "Trying to download: ${ARCHIVE_URL}"

    # Check if file exists (HEAD request)
    if curl -s --head --fail "${ARCHIVE_URL}" >/dev/null 2>&1; then
        break
    else
        info "Not found: ${ARCHIVE_NAME}"
        ARCHIVE_NAME=""
    fi
done

[ -z "$ARCHIVE_NAME" ] && fatal "No matching archive (.tar.gz / .zip) found for ${OS}-${ARCH}"

# ---------------------------------------------
#      Download and installation
# ---------------------------------------------
info "Installing ${PROG} ${VERSION} (${OS}/${ARCH})..."

if command -v curl >/dev/null 2>&1; then
    DL="curl -Lfs"
elif command -v wget >/dev/null 2>&1; then
    DL="wget -qO-"
else
    fatal "You need curl or wget"
fi

TMP_DIR=$(mktemp -d /tmp/qa-install.XXXXXXXX)
trap 'rm -rf "$TMP_DIR"' EXIT

$DL "${ARCHIVE_URL}" > "$TMP_DIR/${ARCHIVE_NAME}" || fatal "Download error"

cd "$TMP_DIR"

# Extraction
case "$ARCHIVE_NAME" in
    *.tar.gz) tar -xzf "${ARCHIVE_NAME}" || fatal "Error extracting tar.gz" ;;
    *.zip)
        if command -v unzip >/dev/null 2>&1; then
            unzip -q "${ARCHIVE_NAME}" || fatal "Error extracting zip"
        else
            fatal "unzip not found – install unzip and try again"
        fi
        ;;
    *) fatal "Unknown archive format" ;;
esac

[ -f "./qa" ] || fatal "No binary file named 'qa' in archive"

chmod 0755 "./qa"

if [ -w "$INSTALL_DIR" ]; then
    mv "./qa" "${INSTALL_DIR}/${PROG}"
else
    sudo mv "./qa" "${INSTALL_DIR}/${PROG}" || fatal "No write permissions for ${INSTALL_DIR}"
fi

success "Installed ${PROG} → ${INSTALL_DIR}/${PROG}"
info "Version:"
${INSTALL_DIR}/${PROG} --version 2>/dev/null || ${INSTALL_DIR}/${PROG} version 2>/dev/null || true

echo
echo "Done! Run:"
echo "   qa --help"
echo
