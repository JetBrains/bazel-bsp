package org.jetbrains.bsp.bazel.projectview.parser

import io.vavr.control.Try
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
     * Parses file under `projectViewFilePath` using file under `
     * defaultProjectViewFilePath` as a default.
     *
     * @param projectViewFilePath path to file with project view
     * @param defaultProjectViewFilePath path to file with default project view
     * @return
     *
     * `Try.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) files under `projectViewFilePath` and `defaultProjectViewFilePath
    ` *  were successfully parsed (not all values have to be provided -- some fields in
     * `ProjectView` might be `Optional.empty`). <br></br>
     * File under `projectViewFilePath` can contain all values, then `
     * defaultProjectViewFilePath` won't be used, or file under `projectViewFilePath
    ` *  can be empty, then all values from file under `defaultProjectViewFilePath
    ` *  will be used, any other configuration is possible as well.
     *
     * 2) file under `projectViewFilePath` doesn't exist, then all values from
     * `defaultProjectViewFilePath` will be used.<br></br>
     * <br></br>
     *
     * `Try.failure` with if:
     *
     * 1) file under `defaultProjectViewFilePath` doesn't exist
     *
     * 2) any other fail happen
     */
    fun parse(projectViewFilePath: Path, defaultProjectViewFilePath: Path): Try<ProjectView>

    /**
     * Parses `projectViewFileContent` using `defaultProjectViewFileContent` as
     * a default.
     *
     * @param projectViewFileContent string with project view
     * @param defaultProjectViewFileContent string with default project view
     * @return
     *
     * `Try.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) `projectViewFileContent` and `defaultProjectViewFileContent
    ` *  were successfully parsed (not all values have to be provided -- some fields in
     * `ProjectView` might be `Optional.empty`). <br></br>
     * `projectViewFileContent` can contain all values, then `
     * defaultProjectViewFileContent` won't be used, `projectViewFileContent
    ` *  can be empty, then all values from `defaultProjectViewFileContent` will
     * be used, any other configuration is possible as well.<br></br>
     * <br></br>
     *
     * `Try.failure` with if:
     *
     * 1) any fail happen
     */
    fun parse(projectViewFileContent: String, defaultProjectViewFileContent: String): Try<ProjectView>

    /**
     * Parses file under `projectViewFilePath`.
     *
     * @param projectViewFilePath path to file with project view
     * @return
     *
     * `Try.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) file under `projectViewFilePath` was successfully parsed (not all values
     * have to be provided -- some fields in `ProjectView` might be `
     * Optional.empty`). <br></br>
     *
     * `Try.failure` with if:
     *
     * 1) file under `projectViewFilePath` doesn't exist
     *
     * 2) any other fail happen
     */
    fun parse(projectViewFilePath: Path): Try<ProjectView>

    /**
     * Parses `projectViewFileContent`.
     *
     * @param projectViewFileContent string with project view
     * @return
     *
     * `Try.success` with `ProjectView` if parsing has finished with
     * success, it means:
     *
     * 1) `projectViewFileContent` was successfully parsed (not all values have to
     * be provided -- some fields in `ProjectView` might be `Optional.empty
    ` * ). <br></br>
     *
     * `Try.failure` with if:
     *
     * 1) any fail happen
     */
    fun parse(projectViewFileContent: String): Try<ProjectView>
}
