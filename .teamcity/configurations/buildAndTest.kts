package configurations

object BuildAndTestTheProject : BaseConfiguration.BaseBazelBuildTypeClean(
    name = "[build & test] build the project & run unit tests",
    command1 = "build",
    targets1 = "//...",
    arguments1 = "",
    command2 = "test",
    targets2 = "//...",
    arguments2 = "--cache_test_results=no",
)
