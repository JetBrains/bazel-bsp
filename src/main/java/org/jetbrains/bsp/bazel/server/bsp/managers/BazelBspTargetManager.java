package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bsp.utils.BuildManagerParsingUtils;

public class BazelBspTargetManager {
  private final BazelBspScalaTargetManager bazelBspScalaTargetManager;
  private final BazelBspJvmTargetManager bazelBspJvmTargetManager;
  private final BazelCppTargetManager bazelCppTargetManager;

  public BazelBspTargetManager(
      BazelRunner bazelRunner,
      BazelBspAspectsManager bazelBspAspectsManager,
      BazelCppTargetManager bazelCppTargetManager) {
    this.bazelBspScalaTargetManager = new BazelBspScalaTargetManager(bazelBspAspectsManager);
    this.bazelCppTargetManager = bazelCppTargetManager;
    this.bazelBspJvmTargetManager =
        new BazelBspJvmTargetManager(bazelRunner, bazelBspAspectsManager);
  }

  private Optional<ScalaBuildTarget> getScalaBuildTarget(Build.Rule rule) {
    return bazelBspScalaTargetManager
        .getValue()
        .map(
            target -> {
              target.setJvmBuildTarget(bazelBspJvmTargetManager.getJVMBuildTarget(rule));
              return target;
            });
  }

  public void fillTargetData(
      BuildTarget target, Set<String> extensions, String ruleClass, Build.Rule rule) {
    if (extensions.contains(Constants.SCALA)) {
      getScalaBuildTarget(rule)
          .ifPresent(
              (buildTarget) -> {
                target.setDataKind(BuildTargetDataKind.SCALA);
                target.setTags(Lists.newArrayList(BuildManagerParsingUtils.getRuleType(ruleClass)));
                target.setData(buildTarget);
              });
    } else if (extensions.contains(Constants.JAVA) || extensions.contains(Constants.KOTLIN)) {
      target.setDataKind(BuildTargetDataKind.JVM);
      target.setTags(Lists.newArrayList(BuildManagerParsingUtils.getRuleType(ruleClass)));
      target.setData(bazelBspJvmTargetManager.getJVMBuildTarget(rule));
    } else if (extensions.contains(Constants.CPP)) {
      bazelCppTargetManager
          .getValue()
          .ifPresent(
              buildTarget -> {
                target.setDataKind(BuildTargetDataKind.CPP);
                target.setTags(Lists.newArrayList(BuildManagerParsingUtils.getRuleType(ruleClass)));
                target.setData(buildTarget);
              });
    }
  }

  public BazelBspScalaTargetManager getBazelBspScalaTargetManager() {
    return bazelBspScalaTargetManager;
  }

  public BazelBspJvmTargetManager getBazelBspJvmTargetManager() {
    return bazelBspJvmTargetManager;
  }
}
