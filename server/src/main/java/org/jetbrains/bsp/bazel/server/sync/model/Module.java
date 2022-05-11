package org.jetbrains.bsp.bazel.server.sync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;

public class Module {
  private final Label label;
  private final boolean isSynthetic;
  private final Seq<Label> directDependencies;
  private final Set<Language> languages;
  private final Set<Tag> tags;
  private final URI baseDirectory;
  private final SourceSet sourceSet;
  private final Set<URI> resources;
  private final Set<URI> sourceDependencies;
  private final Option<LanguageData> languageData;

  public Module(
      @JsonProperty("label") Label label,
      @JsonProperty("synthetic") boolean isSynthetic,
      @JsonProperty("directDependencies") Seq<Label> directDependencies,
      @JsonProperty("languages") Set<Language> languages,
      @JsonProperty("tags") Set<Tag> tags,
      @JsonProperty("baseDirectory") URI baseDirectory,
      @JsonProperty("sourceSet") SourceSet sourceSet,
      @JsonProperty("resources") Set<URI> resources,
      @JsonProperty("sourceDependencies") Set<URI> sourceDependencies,
      @JsonProperty("languageData") Option<LanguageData> languageData) {
    this.label = label;
    this.isSynthetic = isSynthetic;
    this.directDependencies = directDependencies;
    this.languages = languages;
    this.tags = tags;
    this.baseDirectory = baseDirectory;
    this.sourceSet = sourceSet;
    this.resources = resources;
    this.sourceDependencies = sourceDependencies;
    this.languageData = languageData;
  }

  public Label label() {
    return label;
  }

  // TODO do not build synthetic modules
  public boolean isSynthetic() {
    return isSynthetic;
  }

  public Seq<Label> directDependencies() {
    return directDependencies;
  }

  public Set<Language> languages() {
    return languages;
  }

  public Set<Tag> tags() {
    return tags;
  }

  public URI baseDirectory() {
    return baseDirectory;
  }

  public SourceSet sourceSet() {
    return sourceSet;
  }

  public Set<URI> resources() {
    return resources;
  }

  public Set<URI> sourceDependencies() {
    return sourceDependencies;
  }

  public Option<LanguageData> languageData() {
    return languageData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Module module = (Module) o;
    return isSynthetic == module.isSynthetic
        && label.equals(module.label)
        && directDependencies.equals(module.directDependencies)
        && languages.equals(module.languages)
        && tags.equals(module.tags)
        && baseDirectory.equals(module.baseDirectory)
        && sourceSet.equals(module.sourceSet)
        && resources.equals(module.resources)
        && sourceDependencies.equals(module.sourceDependencies)
        && languageData.equals(module.languageData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        label,
        isSynthetic,
        directDependencies,
        languages,
        tags,
        baseDirectory,
        sourceSet,
        resources,
        sourceDependencies,
        languageData);
  }

  @Override
  public String toString() {
    return Format.object(
        "Module",
        Format.entry("label", label),
        Format.entry("isSynthetic", isSynthetic),
        Format.entry("directDependencies", Format.iterable(directDependencies)),
        Format.entry("languages", Format.iterableShort(languages)),
        Format.entry("tags", Format.iterableShort(tags)),
        Format.entry("baseDirectory", baseDirectory),
        Format.entry("sourceSet", sourceSet),
        Format.entry("resources", Format.iterableShort(resources)),
        Format.entry("sourceDependencies", Format.iterable(sourceDependencies)),
        Format.entry("languageData", languageData));
  }
}
