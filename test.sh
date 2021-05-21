#!/usr/bin/env bash

dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
echo $dir
. "${dir}"/test_runner.sh

test_bsp_server() {
  bazel build //main/src/org/jetbrains/bsp/bazel:bsp-install
  bsp_path="$(bazel info bazel-bin)/main/src/org/jetbrains/bsp/bazel/bsp-install"
  cd sample-repo
  $bsp_path
  cd ..
  bazel run //main/test/org/jetbrains/bsp/bazel:bsp-test
}

run_test test_bsp_server
