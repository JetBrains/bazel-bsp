package org.jetbrains.bsp.bazel.server.sync;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext;

public class IntelliJProjectTreeViewFix {
  public List<Module> createModules(
      URI workspaceRoot, List<Module> modules, WorkspaceContext workspaceContext) {
    if (isFullWorkspaceImport(workspaceContext)) {
      return createWorkspaceRootModule(workspaceRoot, modules);
    } else {
      return createMultipleModules(workspaceContext, workspaceRoot, modules);
    }
  }

  private boolean isFullWorkspaceImport(WorkspaceContext workspaceContext) {
    return importTargetSpecs(workspaceContext).exists(s -> s.startsWith("//..."));
  }

  private List<Module> createWorkspaceRootModule(URI workspaceRoot, List<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    if (existingRootDirectories.contains(workspaceRoot)) {
      return List.empty();
    }

    var rootModule = syntheticModule("bsp-workspace-root", workspaceRoot);
    return List.of(rootModule);
  }

  private List<Module> createMultipleModules(
      WorkspaceContext workspaceContext, URI workspaceRoot, List<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    var expectedRootDirs = resolveExpectedRootDirs(workspaceContext, workspaceRoot);
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

  private List<URI> resolveExpectedRootDirs(WorkspaceContext workspaceContext, URI workspaceRoot) {
    var dirs = rootDirsFromTargetSpecs(workspaceContext, workspaceRoot).sorted();
    if (dirs.isEmpty()) return List.empty();
    return removeAlreadyIncludedSubdirs(dirs);
  }

  private List<URI> removeAlreadyIncludedSubdirs(List<String> dirs) {
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

  private List<String> rootDirsFromTargetSpecs(
      WorkspaceContext workspaceContext, URI workspaceRoot) {
    var root = Paths.get(workspaceRoot);
    return importTargetSpecs(workspaceContext)
        .map(s -> stripSuffixes(s, ":all", "...", "/"))
        .map(s -> stripPrefixes(s, "//"))
        .map(root::resolve)
        .filter(Files::exists)
        .map(Path::toUri)
        .map(URI::toString);
  }

  private String stripSuffixes(String s, String... suffixes) {
    for (var suffix : suffixes) {
      if (s.endsWith(suffix)) {
        s = s.substring(0, s.length() - suffix.length());
      }
    }
    return s;
  }

  private String stripPrefixes(String s, String... prefixes) {
    for (var prefix : prefixes) {
      if (s.startsWith(prefix)) {
        s = s.substring(prefix.length());
      }
    }
    return s;
  }

  private List<String> importTargetSpecs(WorkspaceContext workspaceContext) {
    var stream =
        Option.of(workspaceContext.getTargets())
            .toList()
            .flatMap(TargetsSpec::getValues)
            .map(BuildTargetIdentifier::getUri);
    return List.ofAll(stream);
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
