#!/usr/bin/env bash

NC='\033[0m'
GREEN='\033[0;32m'
RED='\033[0;31m'

log_test_progress() {
  echo -e "\n[TEST] $*"
}

run_test() {
  set +e

  SECONDS=0
  TEST_ARG=$*

  log_test_progress "Running \"$TEST_ARG\""

  $TEST_ARG
  EXECUTION_CODE=$?
  DURATION=$SECONDS

  if [ $EXECUTION_CODE -eq 0 ]; then
    log_test_progress "${GREEN}Test \"$TEST_ARG\" successful ($DURATION sec) $NC"
  else
    log_test_progress "${RED}Test \"$TEST_ARG\" failed $NC ($DURATION sec) $NC"
    exit $EXECUTION_CODE
  fi

  set -e
}

test_bsp_server() {
  log_test_progress "Publishing project..."

  bazel run --define "maven_repo=file://$HOME/.m2/repository" //:bsp.publish
  bsp_path="$(bazel info bazel-bin)/bsp-project.jar"
  cd sample-repo

  log_test_progress "Installing BSP..."
  java -cp "$bsp_path" org.jetbrains.bsp.bazel.install.Install
  cd ..

  log_test_progress "Environment has been prepared!"
  log_test_progress "Running integration test..."
  bazel run //src/test/java/org/jetbrains/bsp/bazel:bsp-integration-test
}


log_test_progress "Running BSP tests..."
echo -e "===================================\n"

run_test test_bsp_server

echo -e "\n\n==================================="
echo -e "==================================="
log_test_progress "${GREEN}All test passed!"
