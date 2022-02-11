package org.jetbrains.bsp.bazel.projectview.parser;

import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.commons.BetterFiles;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

/**
 * Project view file parser. Its purpose is to parse *.bazelproject file and create an instance of
 * ProjectView.
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewParser {

  /**
   * Parses file under <code>projectViewFilePath</code> using file under <code>
   * defaultProjectViewFilePath</code> as a default.
   *
   * @param projectViewFilePath path to file with project view
   * @param defaultProjectViewFilePath path to file with default project view
   * @return
   *     <p><code>Try.success</code> with <code>ProjectView</code> if parsing has finished with
   *     success, it means:
   *     <p>1) files under <code>projectViewFilePath</code> and <code>defaultProjectViewFilePath
   *     </code> were successfully parsed (not all values have to be provided -- some fields in
   *     <code>ProjectView</code> might be <code>Optional.empty</code>). <br>
   *     File under <code>projectViewFilePath</code> can contain all values, then <code>
   *     defaultProjectViewFilePath</code> won't be used, or file under <code>projectViewFilePath
   *     </code> can be empty, then all values from file under <code>defaultProjectViewFilePath
   *     </code> will be used, any other configuration is possible as well.
   *     <p>2) file under <code>projectViewFilePath</code> doesn't exist, then all values from
   *     <code>defaultProjectViewFilePath</code> will be used.<br>
   *     <br>
   *     <p><code>Try.failure</code> with if:
   *     <p>1) file under <code>defaultProjectViewFilePath</code> doesn't exist
   *     <p>2) any other fail happen
   */
  default Try<ProjectView> parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    return BetterFiles.tryReadFileContent(defaultProjectViewFilePath)
        .flatMap(
            defaultProjectViewFileContent ->
                parseWithDefault(projectViewFilePath, defaultProjectViewFileContent));
  }

  private Try<ProjectView> parseWithDefault(
      Path projectViewFilePath, String defaultProjectViewFileContent) {
    return BetterFiles.tryReadFileContent(projectViewFilePath)
        .flatMap(
            projectViewFilePathContent ->
                parse(projectViewFilePathContent, defaultProjectViewFileContent))
        .orElse(parse(defaultProjectViewFileContent));
  }

  /**
   * Parses <code>projectViewFileContent</code> using <code>defaultProjectViewFileContent</code> as
   * a default.
   *
   * @param projectViewFileContent string with project view
   * @param defaultProjectViewFileContent string with default project view
   * @return
   *     <p><code>Try.success</code> with <code>ProjectView</code> if parsing has finished with
   *     success, it means:
   *     <p>1) <code>projectViewFileContent</code> and <code>defaultProjectViewFileContent
   *     </code> were successfully parsed (not all values have to be provided -- some fields in
   *     <code>ProjectView</code> might be <code>Optional.empty</code>). <br>
   *     <code>projectViewFileContent</code> can contain all values, then <code>
   *     defaultProjectViewFileContent</code> won't be used, <code>projectViewFileContent
   *     </code> can be empty, then all values from <code>defaultProjectViewFileContent</code> will
   *     be used, any other configuration is possible as well.<br>
   *     <br>
   *     <p><code>Try.failure</code> with if:
   *     <p>1) any fail happen
   */
  Try<ProjectView> parse(String projectViewFileContent, String defaultProjectViewFileContent);

  /**
   * Parses file under <code>projectViewFilePath</code>.
   *
   * @param projectViewFilePath path to file with project view
   * @return
   *     <p><code>Try.success</code> with <code>ProjectView</code> if parsing has finished with
   *     success, it means:
   *     <p>1) file under <code>projectViewFilePath</code> was successfully parsed (not all values
   *     have to be provided -- some fields in <code>ProjectView</code> might be <code>
   *     Optional.empty</code>). <br>
   *     <p><code>Try.failure</code> with if:
   *     <p>1) file under <code>projectViewFilePath</code> doesn't exist
   *     <p>2) any other fail happen
   */
  default Try<ProjectView> parse(Path projectViewFilePath) {
    return BetterFiles.tryReadFileContent(projectViewFilePath).flatMap(this::parse);
  }

  /**
   * Parses <code>projectViewFileContent</code>.
   *
   * @param projectViewFileContent string with project view
   * @return
   *     <p><code>Try.success</code> with <code>ProjectView</code> if parsing has finished with
   *     success, it means:
   *     <p>1) <code>projectViewFileContent</code> was successfully parsed (not all values have to
   *     be provided -- some fields in <code>ProjectView</code> might be <code>Optional.empty
   *     </code>). <br>
   *     <p><code>Try.failure</code> with if:
   *     <p>1) any fail happen
   */
  Try<ProjectView> parse(String projectViewFileContent);
}
