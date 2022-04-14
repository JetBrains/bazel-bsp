package org.jetbrains.bsp.bazel.install.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import io.vavr.collection.List;

public class CliOptionsProviderTest {

  @Test
  public void shouldReturnSuccessIfWorkspaceRootDirIsNotEmpty() {
    // given
    var args = new String[] {"-d", "/path/to/dir"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedWorkspaceRootDir = Paths.get("/path/to/dir");
    assertThat(cliOptions.getWorkspaceRootDir()).isEqualTo(expectedWorkspaceRootDir);
  }

  @Test
  public void shouldReturnSuccessIfWorkspaceRootDirIsEmpty() {
    // given
    var args = new String[] {};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedWorkspaceRootDir = Paths.get("");
    assertThat(cliOptions.getWorkspaceRootDir()).isEqualTo(expectedWorkspaceRootDir);
  }

  @Test
  public void shouldReturnSuccessIfProjectViewFilePathIsEmpty() {
    // given
    var args = new String[] {};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedProjectViewFilePath = Option.none();
    assertThat(cliOptions.getProjectViewFilePath()).isSameAs(expectedProjectViewFilePath);
  }

  @Test
  public void shouldReturnSuccessIfAbsolutePathOfProjectViewFilePathIsProvidedAndIsTrue() {
    // given
    var args = new String[] {"-p", "path/to/dir"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedProjectViewFilePath = Paths.get("path/to/dir").normalize().toAbsolutePath();
    assertThat(cliOptions.getProjectViewFilePath()).isNotEmpty();
    assertThat(cliOptions.getProjectViewFilePath().get().isAbsolute()).isTrue();
    assertThat(cliOptions.getProjectViewFilePath()).endsWith(expectedProjectViewFilePath);
  }

  @Test
  public void shouldReturnSuccessIfRelativePathOfProjectViewFilePathIsProvidedAndIsTrue() {
    // given
    var args = new String[] {"-p", "/path/to/dir"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedProjectViewFilePath = Paths.get("path/to/dir");
    assertThat(cliOptions.getProjectViewFilePath()).isNotEmpty();
    assertThat(cliOptions.getProjectViewFilePath().get().isAbsolute()).isTrue();
    assertThat(cliOptions.getProjectViewFilePath().get().endsWith(expectedProjectViewFilePath))
        .isTrue();
  }

  @Test
  public void shouldReturnSuccessIfJavaPathIsProvidedAndIsTrue() {
    // given
    var args = new String[] {"-j", "/path/to/dir"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedJavaPath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getJavaPath()).containsExactly(expectedJavaPath);
  }

  @Test
  public void shouldReturnSuccessIfBazelPathIsProvidedAndIsTrue() {
    // given
    var args = new String[] {"-b", "/path/to/dir"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedBazelPath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getBazelPath()).containsExactly(expectedBazelPath);
  }

  @Test
  public void shouldReturnSuccessIfDebuggerAddressIsProvidedAndIsTrue() {
    // given
    var args = new String[] {"-x", "host:8000"};

    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedDebuggerAddress = HostAndPort.fromString("host:8000");
    assertThat(cliOptions.getDebuggerAddress()).containsExactly(expectedDebuggerAddress);
  }

  @Test
  public void shouldReturnSuccessIfTargetsAreProvidedAndAreTrue() {
    // given
    var args = new String[] {"-t", "target1", "target2", "target3"};
    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedTargets = List.of("target1", "target2", "target3");
    assertThat(cliOptions.getTargets()).containsExactly(expectedTargets);
  }

  @Test
  public void shouldReturnSuccessIfBuildFlagsAreProvidedAndAreTrue() {
    // given
    var args = new String[] {"-f", "buildFlag1", "buildFlag2", "buildFlag3"};
    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedBuildFlags = List.of("buildFlag1", "buildFlag2", "buildFlag3");
    assertThat(cliOptions.getBuildFlags()).containsExactly(expectedBuildFlags);
  }

  @Test
  public void shouldReturnSuccessIfAllFlagsAreProvidedAndAreTrue() {
    // given
    var args =
        new String[] {
          "-d",
          "/path/to/dir",
          "-p",
          "/path/to/dir",
          "-j",
          "/path/to/dir",
          "-b",
          "/path/to/dir",
          "-x",
          "host:8000",
          "-t",
          "target1",
          "target2",
          "target3",
          "-f",
          "buildFlag1",
          "buildFlag2",
          "buildFlag3"
        };
    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedWorkspaceRootDir = Paths.get("/path/to/dir");
    assertThat(cliOptions.getWorkspaceRootDir()).isEqualTo(expectedWorkspaceRootDir);

    var expectedProjectViewFilePath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getProjectViewFilePath()).isNotEmpty();
    assertThat(cliOptions.getProjectViewFilePath().get().isAbsolute()).isTrue();
    assertThat(cliOptions.getProjectViewFilePath()).endsWith(expectedProjectViewFilePath);

    var expectedJavaPath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getJavaPath()).containsExactly(expectedJavaPath);

    var expectedBazelPath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getBazelPath()).containsExactly(expectedBazelPath);

    var expectedDebuggerAddress = HostAndPort.fromString("host:8000");
    assertThat(cliOptions.getDebuggerAddress()).containsExactly(expectedDebuggerAddress);

    var expectedTargets = List.of("target1", "target2", "target3");
    assertThat(cliOptions.getTargets()).containsExactly(expectedTargets);

    var expectedBuildFlags = List.of("buildFlag1", "buildFlag2", "buildFlag3");
    assertThat(cliOptions.getBuildFlags()).containsExactly(expectedBuildFlags);
  }

  @Test
  public void shouldReturnSuccessIfHalfOfFlagsAreProvidedAndAreTrue() {
    // given
    var args =
        new String[] {
          "-d",
          "/path/to/dir",
          "-j",
          "/path/to/dir",
          "-x",
          "host:8000",
          "-f",
          "buildFlag1",
          "buildFlag2",
          "buildFlag3"
        };
    // when
    var provider = new CliOptionsProvider(args);
    var cliOptionsTry = provider.getOptions();

    // then
    assertThat(cliOptionsTry.isSuccess()).isTrue();
    var cliOptions = cliOptionsTry.get();

    var expectedWorkspaceRootDir = Paths.get("/path/to/dir");
    assertThat(cliOptions.getWorkspaceRootDir()).isEqualTo(expectedWorkspaceRootDir);

    var expectedJavaPath = Paths.get("/path/to/dir");
    assertThat(cliOptions.getJavaPath()).containsExactly(expectedJavaPath);

    var expectedDebuggerAddress = HostAndPort.fromString("host:8000");
    assertThat(cliOptions.getDebuggerAddress()).containsExactly(expectedDebuggerAddress);

    var expectedBuildFlags = List.of("buildFlag1", "buildFlag2", "buildFlag3");
    assertThat(cliOptions.getBuildFlags()).containsExactly(expectedBuildFlags);
  }
}
