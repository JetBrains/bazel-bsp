package configurations

open class BuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BaseBazelBuildTypeClean(
    name = "[build] $name",
    command = "build",
    targets = moduleLabel,
)

object BuildTheProject : BuildBuildType(
    name = "build the project",
    moduleLabel = "//...",
)

