package org.jetbrains.bsp.bazel.install.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

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
    assertThat(cliOptions.getProjectViewFilePath().get().endsWith(expectedProjectViewFilePath)).isTrue();
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

//  @Test
//  public void shouldReturnSuccessIfTargetsAreProvidedAndIsTrue() {
//    // given
//    var args = new String[] {"-t", ""};
//
//    // when
//    var provider = new CliOptionsProvider(args);
//    var cliOptionsTry = provider.getOptions();
//
//    // then
//    assertThat(cliOptionsTry.isSuccess()).isTrue();
//    var cliOptions = cliOptionsTry.get();
//
//    var expectedTargets = ;
//    assertThat(cliOptions.getDebuggerAddress()).containsExactly(expectedTargets);
//  }
}
