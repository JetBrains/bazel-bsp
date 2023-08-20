package org.jetbrains.bsp.bazel.projectview.parser

import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

/**
 * Project view file parser. Its purpose is to parse *.bazelproject file and create an instance of
 * ProjectView.
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
interface ProjectViewParser {

    /**
     * Parses file under `projectViewFilePath`.
     *
     * @param projectViewFilePath path to file with project view
     * @return
     *
     * `Result.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) file under `projectViewFilePath` was successfully parsed (not all values
     * have to be provided -- some fields in `ProjectView` might be `
     * Optional.empty`). <br></br>
     *
     * `Result.failure` with if:
     *
     * 1) file under `projectViewFilePath` doesn't exist
     *
     * 2) any other fail happen
     */
    fun parse(projectViewFilePath: Path): Result<ProjectView>

    /**
     * Parses `projectViewFileContent`.
     *
     * @param projectViewFileContent string with project view
     * @return
     *
     * `Result.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) `projectViewFileContent` was successfully parsed (not all values have to
     * be provided -- some fields in `ProjectView` might be `Optional.empty
    ` * ). <br></br>
     *
     * `Result.failure` with if:
     *
     * 1) any fail happen
     */
    fun parse(projectViewFileContent: String): Result<ProjectView>
}
