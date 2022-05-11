package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.junit.jupiter.api.Test;

public class InternalAspectsResolverTest {

  private static BazelInfo createBazelInfo(Path workspaceRoot) {
    return new BasicBazelInfo("", workspaceRoot);
  }

  @Test
  public void shouldResolveLabelForBspRootAtWorkspaceRoot() {
    // given
    var workspaceRoot = "/Users/user/workspace/test-project";
    var bspProjectRoot = workspaceRoot;

    // when
    var internalAspectsResolver = createAspectsResolver(workspaceRoot, bspProjectRoot);
    var aspectLabel = internalAspectsResolver.resolveLabel("get_classpath");

    // then
    assertThat(aspectLabel).isEqualTo("@bazelbsp_aspect//:aspects.bzl%get_classpath");
  }

  @Test
  public void shouldResolveLabelForBspRootInSubdirectoryOfWorkspace() {
    // given
    var workspaceRoot = "/Users/user/workspace/test-project";
    var bspProjectRoot = "/Users/user/workspace/test-project/bsp-projects/test-project-bsp";

    // when
    var internalAspectsResolver = createAspectsResolver(workspaceRoot, bspProjectRoot);
    var aspectLabel = internalAspectsResolver.resolveLabel("get_classpath");

    // then
    assertThat(aspectLabel).isEqualTo("@bazelbsp_aspect//:aspects.bzl%get_classpath");
  }

  private InternalAspectsResolver createAspectsResolver(
      String workspaceRoot, String bspProjectRoot) {
    return new InternalAspectsResolver(
        createBazelInfo(Paths.get(workspaceRoot)), new BspInfo(Paths.get(bspProjectRoot)));
  }
}
