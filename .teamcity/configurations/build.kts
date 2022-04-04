package configurations

open class BuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BaseBazelBuildType(
    name = "[build] $name",
    command = "build",
    targets = moduleLabel,
)

object BuildTheProject : BuildBuildType(
    name = "build the project",
    moduleLabel = "//...",
)