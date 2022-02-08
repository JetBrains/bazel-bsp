package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class IntelliJProjectTreeViewFix {
  public List<Module> createModules(
      URI workspaceRoot, List<Module> modules, ProjectView projectView) {
    if (isFullWorkspaceImport(projectView)) {
      return createWorkspaceRootModule(workspaceRoot, modules);
    } else {
      return createMultipleModules(workspaceRoot, modules);
    }
  }

  private boolean isFullWorkspaceImport(ProjectView projectView) {
    return projectView.getTargets().stream()
        .flatMap(s -> s.getIncludedValues().stream())
        .anyMatch(s -> s.getUri().startsWith("//..."));
  }

  private List<Module> createWorkspaceRootModule(URI workspaceRoot, List<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    if (existingRootDirectories.contains(workspaceRoot)) {
      return List.empty();
    }

    var rootModule = syntheticModule("bsp-workspace-root", workspaceRoot);
    return List.of(rootModule);
  }

  private List<Module> createMultipleModules(URI workspaceRoot, List<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    var expectedRootDirs = resolveExpectedRootDirs(modules);
    return expectedRootDirs.flatMap(
        root -> {
          if (existingRootDirectories.contains(root)) {
            return Option.none();
          }

          var relative = Paths.get(workspaceRoot).relativize(Paths.get(root)).toString();
          var moduleName = relative + "-modules-root";
          return Option.some(syntheticModule(moduleName, root));
        });
  }

  private Set<URI> resolveExistingRootDirectories(List<Module> modules) {
    return modules.toSet().map(Module::sourceSet).flatMap(SourceSet::sourceRoots);
  }

  private List<URI> resolveExpectedRootDirs(List<Module> modules) {
    var dirs = modules.map(Module::baseDirectory).map(URI::toString).sorted();
    if (dirs.isEmpty()) return List.empty();
    var buffer = new ArrayList<String>();
    var current = dirs.get(0);
    buffer.add(current);
    for (var path : dirs.iterator().drop(1)) {
      if (!path.startsWith(current)) {
        current = path;
        buffer.add(current);
      }
    }
    return List.ofAll(buffer.stream().map(URI::create));
  }

  private Module syntheticModule(String moduleName, URI baseDirectory) {
    HashSet<URI> resources = HashSet.of(baseDirectory);
    return new Module(
        Label.from(moduleName),
        true,
        List.empty(),
        HashSet.empty(),
        HashSet.of(Tag.NO_BUILD),
        baseDirectory,
        new SourceSet(HashSet.empty(), HashSet.empty()),
        resources,
        HashSet.empty(),
        Option.none());
  }
}
