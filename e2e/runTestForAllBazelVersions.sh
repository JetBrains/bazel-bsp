# this script installs required environment (building server + installing it in the given directory)
# and then runs test itself for all listed bazel versions

if [ "$#" -ne 1 ] && [ "$#" -ne 2 ]; then
  echo "Illegal number of parameters!"
  echo "Usage: ./runTest.sh <test target> [path to the project]"
  exit 1
fi

# the first argument of the script should be a bazel test target for provided test project
TEST_TARGET="$1"

# the second argument (optional) of the script should be a path to the directory with tested project (relative to the project root)
TEST_PROJECT_PATH="$2"

runTest() {
  ./runTest.sh "$TEST_TARGET" "$*" "$TEST_PROJECT_PATH"
  EXECUTION_CODE=$?

  if [ $EXECUTION_CODE -ne 0 ]; then
    exit 1
  fi
}

cd e2e || exit

runTest "1.x"
runTest "2.x"
runTest "3.x"
runTest "4.x"
runTest "5.x"

echo -e "\n==================================="
echo -e "${GREEN}'$TEST_TARGET' for all bazel versions passed!${NC}"
echo -e "==================================="
echo -e "===================================\n\n\n"
