package org.jetbrains.bsp.bazel.server.sync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.commons.Format;

/** Project is the internal model of the project. Bazel/Aspect Model -> Project -> BSP Model */
public class Project {
  private final URI workspaceRoot;
  private final Map<URI, Label> sourceToTarget;
  private final Seq<Module> modules;
  @JsonIgnore private final Map<Label, Module> moduleMap;

  public Project(
      @JsonProperty("workspaceRoot") URI workspaceRoot,
      @JsonProperty("modules") Seq<Module> modules,
      @JsonProperty("sourceToTarget") Map<URI, Label> sourceToTarget) {
    this.workspaceRoot = workspaceRoot;
    this.sourceToTarget = sourceToTarget;
    this.modules = modules;
    this.moduleMap = modules.toMap(Module::label, Function.identity());
  }

  public URI workspaceRoot() {
    return workspaceRoot;
  }

  public Seq<Module> modules() {
    return modules;
  }

  public Option<Module> findModule(Label label) {
    return moduleMap.get(label);
  }

  public Option<Label> findTargetBySource(URI documentUri) {
    return sourceToTarget.get(documentUri);
  }

  public Map<URI, Label> sourceToTarget() {
    return sourceToTarget;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Project project = (Project) o;
    return workspaceRoot.equals(project.workspaceRoot)
        && sourceToTarget.equals(project.sourceToTarget)
        && modules.equals(project.modules)
        && moduleMap.equals(project.moduleMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceRoot, sourceToTarget, modules, moduleMap);
  }

  @Override
  public String toString() {
    return Format.object("Project", Format.entry("workspaceRoot", workspaceRoot));
  }
}
