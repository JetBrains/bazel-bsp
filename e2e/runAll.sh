#!/usr/bin/env bash

GREEN='\033[0;32m'

runTest() {
  bazel run "$*"
  EXECUTION_CODE=$?

  if [ $EXECUTION_CODE -ne 0 ]; then
    exit 1
  fi
}

cd "$BUILD_WORKSPACE_DIRECTORY" || exit

runTest //e2e:BazelBspSampleRepoTest
#runTest //e2e:BazelBspActionGraphV1Test
#runTest //e2e:BazelBspActionGraphV2Test
#runTest //e2e:BazelBspLocalJdkTest
#runTest //e2e:BazelBspRemoteJdkTest
#runTest //e2e:BazelBspCppProjectTest

echo -e "${GREEN}==================================="
echo -e "==================================="
echo -e "       All tests passed!"
echo -e "==================================="
echo -e "==================================="