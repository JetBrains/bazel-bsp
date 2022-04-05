package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext;

public class BspConnectionDetailsCreator {

  public static final String SERVER_CLASS_NAME = "org.jetbrains.bsp.bazel.server.ServerInitializer";

  private final InstallationContext installationContext;
  private final Option<Path> projectViewFilePath;

  public BspConnectionDetailsCreator(
      InstallationContext installationContext, Option<Path> projectViewFilePath) {
    this.installationContext = installationContext;
    this.projectViewFilePath = projectViewFilePath;
  }

  public Try<BspConnectionDetails> create() {
    return calculateArgv()
        .map(
            argv ->
                new BspConnectionDetails(
                    Constants.NAME,
                    argv.asJava(),
                    Constants.VERSION,
                    Constants.BSP_VERSION,
                    Constants.SUPPORTED_LANGUAGES));
  }

  private Try<List<String>> calculateArgv() {
    return Try.success(List.<String>empty())
        .map(this::appendJavaBinary)
        .map(this::appendClasspathFlag)
        .flatMap(this::appendClasspath)
        .map(this::appendDebuggerConnectionIfExists)
        .map(this::appendServerClassName)
        .map(this::appendProjectViewFilePath);
  }

  private List<String> appendJavaBinary(List<String> argv) {
    var javaBinary = installationContext.getJavaPath().getValue().toString();
    return argv.append(javaBinary);
  }

  private List<String> appendClasspathFlag(List<String> argv) {
    var classpathFlag = "-classpath";
    return argv.append(classpathFlag);
  }

  private Try<List<String>> appendClasspath(List<String> argv) {
    return readSystemProperty("java.class.path")
        .map(this::mapClasspathToAbsolutePaths)
        .map(argv::append);
  }

  private Try<String> readSystemProperty(String name) {
    return Option.of(System.getProperty(name))
        .toTry(() -> new NoSuchElementException("Could not read " + name + " system property"));
  }

  private String mapClasspathToAbsolutePaths(String systemPropertyClasspath) {
    return List.of(systemPropertyClasspath.split(":"))
        .map(Paths::get)
        .map(Path::toAbsolutePath)
        .map(Path::toString)
        .collect(Collectors.joining(":"));
  }

  private List<String> appendDebuggerConnectionIfExists(List<String> argv) {
    return installationContext
        .getDebuggerAddress()
        .map(ExecutionContextSingletonEntity::getValue)
        .map(
            debuggerAddress ->
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="
                    + debuggerAddress.toString())
        .map(argv::append)
        .getOrElse(argv);
  }

  private List<String> appendServerClassName(List<String> argv) {
    return argv.append(SERVER_CLASS_NAME);
  }

  private List<String> appendProjectViewFilePath(List<String> argv) {
    return projectViewFilePath.map(Path::toString).map(argv::append).getOrElse(argv);
  }
}
