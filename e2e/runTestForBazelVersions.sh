# this script installs required environment (building server + installing it in the given directory)
# and then runs test itself for all listed bazel versions

# NOTE: this file probably will be removed soon anyway

if [ "$#" -lt 3 ]; then
  echo "Illegal number of parameters!"
  echo "Usage: ./runTestForBazelVersions.sh <test target> <path to the project> [list of bazel versions]"
  exit 1
fi

# the first argument of the script should be a bazel test target for provided test project
TEST_TARGET="$1"

# the second argument of the script should be a path to the directory with tested project (relative to the project root)
TEST_PROJECT_PATH="$2"

runTest() {
  BAZEL_VERSION="$*"
  BAZELRC_FILE_PATH="../.emptyrc"

  if [ "$BAZEL_VERSION" == "5.x" ] && [ "$TEST_PROJECT_PATH" == "e2e/test-resources/java-8-project" ]; then
    BAZELRC_FILE_PATH="../.java8bazel5rc"
  elif [ "$BAZEL_VERSION" != "5.x" ] && [ "$TEST_PROJECT_PATH" == "e2e/test-resources/java-8-project" ]; then
    BAZELRC_FILE_PATH="../.java8bazel1234rc"
  fi

  if [ "$BAZEL_VERSION" == "5.x" ] && [ "$TEST_PROJECT_PATH" == "e2e/test-resources/java-11-project" ]; then
    BAZELRC_FILE_PATH="../.java11bazel5rc"
  elif [ "$BAZEL_VERSION" != "5.x" ] && [ "$TEST_PROJECT_PATH" == "e2e/test-resources/java-11-project" ]; then
    BAZELRC_FILE_PATH="../.java11bazel1234rc"
  fi

  ./runTest.sh "$TEST_TARGET" "$TEST_PROJECT_PATH" "$BAZELRC_FILE_PATH" "$*"
  EXECUTION_CODE=$?

  if [ $EXECUTION_CODE -ne 0 ]; then
    exit 1
  fi
}

cd e2e || exit

for i in "${@:3}"; do
  runTest "$i"
done

echo -e "\n==================================="
echo -e "${GREEN}'$TEST_TARGET' for all bazel versions passed!${NC}"
echo -e "==================================="
echo -e "===================================\n\n\n"
