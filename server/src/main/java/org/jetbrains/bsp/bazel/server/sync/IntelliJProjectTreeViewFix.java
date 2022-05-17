package org.jetbrains.bsp.bazel.server.sync;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.Array;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
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
  public Seq<Module> createModules(
      URI workspaceRoot, Seq<Module> modules, WorkspaceContext workspaceContext) {
    if (isFullWorkspaceImport(workspaceContext)) {
      return createWorkspaceRootModule(workspaceRoot, modules);
    } else {
      return createMultipleModules(workspaceContext, workspaceRoot, modules);
    }
  }

  private boolean isFullWorkspaceImport(WorkspaceContext workspaceContext) {
    return importTargetSpecs(workspaceContext).exists(s -> s.startsWith("//..."));
  }

  private Seq<Module> createWorkspaceRootModule(URI workspaceRoot, Seq<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    if (existingRootDirectories.contains(workspaceRoot)) {
      return Array.empty();
    }

    var rootModule = syntheticModule("bsp-workspace-root", workspaceRoot);
    return Array.of(rootModule);
  }

  private Seq<Module> createMultipleModules(
      WorkspaceContext workspaceContext, URI workspaceRoot, Seq<Module> modules) {
    var existingRootDirectories = resolveExistingRootDirectories(modules);
    var expectedRootDirs = resolveExpectedRootDirs(workspaceContext, workspaceRoot);
    var workspaceRootPath = Paths.get(workspaceRoot);
    return expectedRootDirs.flatMap(
        root -> {
          if (existingRootDirectories.contains(root)) {
            return Option.none();
          }

          var relative = workspaceRootPath.relativize(Paths.get(root)).toString();
          var moduleName = relative + "-modules-root";
          return Option.some(syntheticModule(moduleName, root));
        });
  }

  private Set<URI> resolveExistingRootDirectories(Seq<Module> modules) {
    return modules.iterator().map(Module::sourceSet).flatMap(SourceSet::sourceRoots).toSet();
  }

  private Seq<URI> resolveExpectedRootDirs(WorkspaceContext workspaceContext, URI workspaceRoot) {
    var dirs = rootDirsFromTargetSpecs(workspaceContext, workspaceRoot).sorted();
    if (dirs.isEmpty()) return List.empty();
    return removeAlreadyIncludedSubdirs(dirs);
  }

  private Seq<URI> removeAlreadyIncludedSubdirs(Seq<String> dirs) {
    var buffer = new ArrayList<String>();
    var current = dirs.get(0);
    buffer.add(current);
    for (var path : dirs.iterator().drop(1)) {
      if (!path.startsWith(current)) {
        current = path;
        buffer.add(current);
      }
    }
    return Array.ofAll(buffer.stream().map(URI::create));
  }

  private Seq<String> rootDirsFromTargetSpecs(
      WorkspaceContext workspaceContext, URI workspaceRoot) {
    var root = Paths.get(workspaceRoot);
    return importTargetSpecs(workspaceContext)
        .map(s -> stripSuffixes(s, ":all", "...", "/"))
        .map(s -> stripPrefixes(s, "//"))
        .map(root::resolve)
        .filter(Files::exists)
        .map(Path::toUri)
        .map(URI::toString)
        .toArray();
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

  private Iterator<String> importTargetSpecs(WorkspaceContext workspaceContext) {
    return Option.of(workspaceContext.getTargets())
        .iterator()
        .flatMap(TargetsSpec::getValues)
        .map(BuildTargetIdentifier::getUri);
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
