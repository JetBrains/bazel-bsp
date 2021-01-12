package org.jetbrains.bsp.bazel.server.bsp.services;

import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.TargetsResolver;

public class JavaBuildServerService {

  private static final String JAVA_COMPILER_OPTIONS_NAME = "javacopts";
  // TODO(andrefmrocha): Remove this when kotlin is natively supported
  private static final List<String> JAVA_LANGUAGES_IDS = ImmutableList.of(Constants.JAVAC, Constants.KOTLINC);

  private final TargetsResolver<JavacOptionsItem> targetsResolver;

  public JavaBuildServerService(
      BazelData bazelData,
      BazelRunner bazelRunner) {
    this.targetsResolver =
        TargetsResolver.<JavacOptionsItem>builder()
          .bazelData(bazelData)
          .bazelRunner(bazelRunner)
          .compilerOptionsName(JAVA_COMPILER_OPTIONS_NAME)
          .languagesIds(JAVA_LANGUAGES_IDS)
          .resultItemsCollector(JavacOptionsItem::new)
          .build();
  }

  public Either<ResponseError, JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    List<JavacOptionsItem> resultItems = targetsResolver.getResultItemsForTargets(javacOptionsParams.getTargets());

    JavacOptionsResult javacOptionsResult = new JavacOptionsResult(resultItems);
    return Either.forRight(javacOptionsResult);
  }

}
