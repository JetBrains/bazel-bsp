#!/usr/bin/env bash

# this script installs required environment (building server + installing it in the given directory)
# and then runs test itself

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters!"
    echo "Usage: ./runTest.sh <path to the project> <test target>"
    exit 1
fi

# first argument of the script should be a path to the directory with tested project (relative to the project root)
TEST_PROJECT_PATH="$1"

# second argument of the script should be a bazel test target for provided test project
TEST_TARGET="$2"

echo -e "Running BSP test for '$TEST_PROJECT_PATH'..."
echo -e "===================================\n"

echo "Building project..."
cd "$BUILD_WORKSPACE_DIRECTORY" || exit
bazel build //src/main/java/org/jetbrains/bsp/bazel:bsp-install
bsp_path="$(bazel info bazel-bin)/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo "Building done."

echo "Installing BSP..."
cd "$TEST_PROJECT_PATH" || exit
$bsp_path
echo "Installing done."
echo "Environment has been prepared!"
echo -e "===================================\n"

cd "$BUILD_WORKSPACE_DIRECTORY" || exit
bazel run "$TEST_TARGET"