#!/usr/bin/env bash

dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
echo $dir
. "${dir}"/test_runner.sh

test_bsp_server() {
  bazel run --define "maven_repo=file://$HOME/.m2/repository" //:bsp.publish
  bsp_path="$(bazel info bazel-bin)/bsp-project.jar"
  cd sample-repo
  java -cp $bsp_path org.jetbrains.bsp.bazel.Install
  cd ..
  bazel run //main/test/org/jetbrains/bsp/bazel:bsp-test
}

run_test test_bsp_server