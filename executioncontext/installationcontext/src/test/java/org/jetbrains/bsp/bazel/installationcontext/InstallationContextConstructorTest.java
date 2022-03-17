package org.jetbrains.bsp.bazel.installationcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.junit.Before;
import org.junit.Test;

public class InstallationContextConstructorTest {

  private InstallationContextConstructor installationContextConstructor;

  @Before
  public void beforeEach() {
    // given
    this.installationContextConstructor = new InstallationContextConstructor();
  }

  // Try<WorkspaceContext> construct(Try<ProjectView> projectViewTry)

  @Test
  public void shouldReturnFailureIfProjectViewIsFailure() {
    // given
    var projectViewTry = Try.<ProjectView>failure(new Exception("exception message"));

    // when
    var installationContextTry = installationContextConstructor.construct(projectViewTry);

    // then
    assertTrue(installationContextTry.isFailure());
    assertEquals(Exception.class, installationContextTry.getCause().getClass());
    assertEquals("exception message", installationContextTry.getCause().getMessage());
  }

  // Try<WorkspaceContext> construct(ProjectView projectView)

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
    assertTrue(installationContextTry.isSuccess());
    var installationContext = installationContextTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertEquals(expectedJavaPath, installationContext.getJavaPath());

    var expectedDebuggerAddress =
        new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000"));
    assertEquals(expectedDebuggerAddress, installationContext.getDebuggerAddress().get());
  }
}
