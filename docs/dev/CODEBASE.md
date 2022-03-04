# Codebase

## Tests

### End-to-end tests

`e2e` directory contains end-2-end tests that check various scenarios of server usage.

- `bazel run //e2e:all` - to run all tests
- `bazel run //e2e:<specific test>` - to run a specific test (to see all possible tests, check the `e2e/BUILD` file)

### Unit tests

Most modules also have unit tests that can be run using `bazel test //<module>/...` or just `bazel test //...` to run
all tests in the project.

## Extending

Do you want to extend the server to other languages? Check [it](EXTENDING.md) out.