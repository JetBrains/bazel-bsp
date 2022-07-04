#!/usr/bin/env bash

# this script installs required environment (building server + installing it in the given directory)
# and then runs test itself

if [ "$#" -ne 1 ] && [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters!"
    echo "Usage: ./runTest.sh <test target> [path to the project]"
    exit 1
fi

# first argument of the script should be a bazel test target for provided test project
TEST_TARGET="$1"

# second argument (optional) of the script should be a path to the directory with tested project (relative to the project root)
TEST_PROJECT_PATH="$2"


GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "==================================="
echo -e "==================================="
echo -e "Running BSP test '$TEST_TARGET' in '$TEST_PROJECT_PATH'..."
echo -e "-----------------------------------\n"

echo "Building project..."
cd "$BUILD_WORKSPACE_DIRECTORY" || exit
bazel build //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install
EXECUTION_CODE=$?
if [ $EXECUTION_CODE -ne 0 ]; then
  echo -e "${RED}building failed :("
  exit 1
fi

bsp_path="$(bazel info bazel-bin)/server/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo "Building done."

echo "Cleaning project directory..."
if [ "$#" -eq 2 ]; then
  cd "$TEST_PROJECT_PATH" || exit
fi
rm -r .bsp/ > /dev/null 2>&1
rm -r .bazelbsp/ > /dev/null 2>&1
echo "Cleaning project directory done!"

echo "Installing BSP..."
$bsp_path || exit
echo "Installing done."
echo "Environment has been prepared!"
echo -e "-----------------------------------\n"

cd "$BUILD_WORKSPACE_DIRECTORY" || exit

foo1=invalid_val1 foo2=invalid_val2 foo3=val3 foo4=val4 bazel run "$TEST_TARGET"
EXECUTION_CODE=$?

if [ $EXECUTION_CODE -ne 0 ]; then
  echo -e "${RED}'$TEST_TARGET' test failed :("
  exit 1
fi

echo -e "\n-----------------------------------"
echo -e "${GREEN}'$TEST_TARGET' passed!${NC}"
echo -e "==================================="
echo -e "===================================\n\n\n"
