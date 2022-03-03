# Execution Context

The Execution Context contains all information needed during execution. It is constructed using the provided Project
View file.

---

## The idea

The idea is to map `ProjectView` into `ExecutionContext` because:

- `ProjectView` contains information used during the installation and the server lifetime - we don't want to keep
  installation information during server runtime, because we don't need it.
- Single `ExecutionContext` entity might be deducted using multiple `ProjectView` sections,
  e.g. `targets` (`ExecutionContext`) is deducted using `targets` (`ProjectView`) section, but also with `directories`
  and `derive_targets_from_directories` flag.

## Flow

The expected flow:

- User specifies path to project view file
- Project view file is parsed using `ExecutionContext` and saved in `ProjectView`
- The `ProjectView` is passed to `ExecutionContextConstructor` and a `ExecutionContext` is constructed
- Later in the code `ExecutionContext` is used to keep execution information
