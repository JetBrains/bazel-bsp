package org.jetbrains.bsp.bazel.server.bloop;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider;

public class BloopExporterInitializer {
  public static void main(String[] args) {
    Option<Path> projectViewPath;
    Path bspProjectRoot = Paths.get("").toAbsolutePath().getParent();
    Path projectRoot;

    if (args.length == 1) {
      projectRoot = Paths.get(args[0]);
      projectViewPath = Option.none();
    } else if (args.length == 2) {
      projectRoot = Paths.get(args[0]);
      projectViewPath = Option.of(Paths.get(args[1]));
    } else {
      System.err.printf("Got too many args: %s%n", Arrays.toString(args));
      System.exit(1);
      throw new RuntimeException();
    }

    boolean hasErrors = false;
    try {
      var bspInfo = new BspInfo(bspProjectRoot);
      var workspaceContextProvider =
          new DefaultWorkspaceContextProvider(projectViewPath.getOrNull());
      new BloopExporter(bspInfo, projectRoot, workspaceContextProvider).export();
    } catch (BloopExporter.BazelExportFailedException ex) {
      hasErrors = true;
      System.err.println(
          "Error exporting bazel project, one more more transitive dependencies failed to build,"
              + " see the previous output for more information:");
      System.err.print('\t');
      System.err.println(ex.getFailedTargets().map(BuildTargetIdentifier::getUri).mkString(", "));
    } catch (Exception ex) {
      hasErrors = true;
      ex.printStackTrace();
    }

    if (hasErrors) {
      System.exit(1);
    } else {
      System.exit(0);
    }
  }
}
