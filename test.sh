dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
echo $dir
. "${dir}"/test_runner.sh

test_bsp_server() {
  bazel build //main/src/org/jetbrains/bsp:bsp_deploy.jar
  bazel run //main/test/org/jetbrains/bsp:bsp-test
}

run_test test_bsp_server