package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspQueryManager;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsLanguageOptionsResolver;

public class JavaBuildServerService {

  private static final Logger LOGGER = LogManager.getLogger(JavaBuildServerService.class);

  private static final String JAVA_COMPILER_OPTIONS_NAME = "javacopts";
  // TODO(andrefmrocha): Remove this when kotlin is natively supported
  private static final List<String> JAVA_LANGUAGES_IDS =
      ImmutableList.of(Constants.JAVAC, Constants.KOTLINC);

  private final BazelBspCompilationManager bazelBspCompilationManager;
  private final BazelBspQueryManager bazelBspQueryManager;

  private final TargetsLanguageOptionsResolver<JavacOptionsItem> targetsLanguageOptionsResolver;

  public JavaBuildServerService(
      BazelBspCompilationManager bazelBspCompilationManager,
      BazelBspQueryManager bazelBspQueryManager,
      BazelData bazelData,
      BazelRunner bazelRunner) {
    this.bazelBspCompilationManager = bazelBspCompilationManager;
    this.bazelBspQueryManager = bazelBspQueryManager;
    this.targetsLanguageOptionsResolver =
        TargetsLanguageOptionsResolver.<JavacOptionsItem>builder()
            .bazelData(bazelData)
            .bazelRunner(bazelRunner)
            .compilerOptionsName(JAVA_COMPILER_OPTIONS_NAME)
            .languagesIds(JAVA_LANGUAGES_IDS)
            .resultItemsCollector(JavacOptionsItem::new)
            .build();
  }

  public Either<ResponseError, JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    LOGGER.info("buildTargetJavacOptions call with param: {}", javacOptionsParams);

    // Make sure dependencies are cached
    List<BuildTargetIdentifier> dependenciesTargets =
        bazelBspQueryManager.getTargetIdentifiersForDependencies(javacOptionsParams.getTargets());
    bazelBspCompilationManager.buildTargetsWithBep(dependenciesTargets, ImmutableList.of());

    List<JavacOptionsItem> resultItems =
        targetsLanguageOptionsResolver.getResultItemsForTargets(javacOptionsParams.getTargets());

    JavacOptionsResult javacOptionsResult = new JavacOptionsResult(resultItems);
    return Either.forRight(javacOptionsResult);
  }
}
