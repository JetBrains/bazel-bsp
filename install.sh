#!/usr/bin/env bash

# script takes the same arguments as installer - check using `--help`

RED="\e[31m"
GREEN="\e[32m"
CLEAR="\e[0m"

# $1 - Error message
# $2 - Success message [optional]
function check_error_code {
  exit_code=$?
  if [ $exit_code -ne 0 ]; then
    echo -e "============================"
    echo -e "${RED}Error${CLEAR}: $1"
    exit $exit_code
  fi
  if [ -n "$2" ]; then
    echo -e "${GREEN}Done${CLEAR}: $2"
  fi
}

echo -e "Building server..."
echo -e "============================"
bazel build //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install
check_error_code "Server build failed!"
bsp_path="$(bazel info bazel-bin)/server/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo -e "============================"
echo -e "Building done."

$bsp_path "$@"
check_error_code "Installation failed!" "Done! Enjoy Bazel BSP!"
