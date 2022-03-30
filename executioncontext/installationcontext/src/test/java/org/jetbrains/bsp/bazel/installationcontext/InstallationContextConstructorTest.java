package org.jetbrains.bsp.bazel.installationcontext;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class InstallationContextConstructorTest {

  private InstallationContextConstructor installationContextConstructor;

  @BeforeEach
  public void beforeEach() {
    // given
    this.installationContextConstructor = new InstallationContextConstructor();
  }

  @Nested
  @DisplayName("Try<WorkspaceContext> construct(projectViewTry) tests")
  class ConstructProjectViewTryTest {

    @Test
    public void shouldReturnFailureIfProjectViewIsFailure() {
      // given
      var projectViewTry = Try.<ProjectView>failure(new Exception("exception message"));

      // when
      var installationContextTry = installationContextConstructor.construct(projectViewTry);

      // then
      assertThat(installationContextTry.isFailure()).isTrue();
      assertThat(installationContextTry.getCause().getClass()).isEqualTo(Exception.class);
      assertThat(installationContextTry.getCause().getMessage()).isEqualTo("exception message");
    }
  }

  @Nested
  @DisplayName("Try<WorkspaceContext> construct(projectView) tests")
  class ConstructProjectViewTest {

    @Test
    public void shouldReturnSuccessIfProjectViewIsValid() {
      // given
      var projectView =
          ProjectView.builder()
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("/path/to/java"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(HostAndPort.fromString("host:8000"))))
              .build();
      // when
      var installationContextTry = installationContextConstructor.construct(projectView);

      // then
      assertThat(installationContextTry.isSuccess()).isTrue();
      var installationContext = installationContextTry.get();

      var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
      assertThat(installationContext.getJavaPath()).isEqualTo(expectedJavaPath);

      var expectedDebuggerAddress =
          new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000"));
      assertThat(installationContext.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddress);
    }
  }
}
