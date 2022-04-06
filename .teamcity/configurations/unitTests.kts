package configurations

open class UnitTestsBuildType(moduleLabel: String) : BaseConfiguration.BaseBazelBuildType(
    name = "[unit tests] $moduleLabel tests",
    command = "test",
    targets = "$moduleLabel/...",
    arguments = "--cache_test_results=no"
)

object BazelRunnerUnitTests : UnitTestsBuildType(
    moduleLabel = "//bazelrunner",
)

object CommonsUnitTests : UnitTestsBuildType(
    moduleLabel = "//commons",
)

object ExecutionContextUnitTests : UnitTestsBuildType(
    moduleLabel = "//executioncontext",
)

object ServerUnitTests : UnitTestsBuildType(
    moduleLabel = "//server",
)
