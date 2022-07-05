package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.junit.jupiter.api.Test;

public class InternalAspectsResolverTest {

  @Test
  public void shouldResolveLabelForBspRootAtWorkspaceRoot() {
    // given
    var bspProjectRoot = "/Users/user/workspace/test-project";

    // when
    var internalAspectsResolver = createAspectsResolver(bspProjectRoot);
    var aspectLabel = internalAspectsResolver.resolveLabel("get_classpath");

    // then
    assertThat(aspectLabel).isEqualTo("@bazelbsp_aspect//:aspects.bzl%get_classpath");
  }

  @Test
  public void shouldResolveLabelForBspRootInSubdirectoryOfWorkspace() {
    // given
    var bspProjectRoot = "/Users/user/workspace/test-project/bsp-projects/test-project-bsp";

    // when
    var internalAspectsResolver = createAspectsResolver(bspProjectRoot);
    var aspectLabel = internalAspectsResolver.resolveLabel("get_classpath");

    // then
    assertThat(aspectLabel).isEqualTo("@bazelbsp_aspect//:aspects.bzl%get_classpath");
  }

  private InternalAspectsResolver createAspectsResolver(String bspProjectRoot) {
    return new InternalAspectsResolver(new BspInfo(Paths.get(bspProjectRoot)));
  }
}
