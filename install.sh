#!/usr/bin/env bash

# script takes the same arguments as installer - check using `--help`

echo -e "Building server..."
echo -e "============================"
bazel build //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install
bsp_path="$(bazel info bazel-bin)/server/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo -e "============================"
echo -e "Building done."

$bsp_path "$@"
exit_code=$?

if [ $exit_code -eq 0 ]; then
  echo -e "Done! Enjoy Bazel BSP!"
else
  echo -e "Installation failed!"
  exit $exit_code
fi
