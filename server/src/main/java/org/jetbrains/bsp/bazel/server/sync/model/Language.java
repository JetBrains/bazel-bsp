package org.jetbrains.bsp.bazel.server.sync.model;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public enum Language {
  SCALA("scala", HashSet.of(".scala")),
  JAVA("java", HashSet.of(".java")),
  KOTLIN("kotlin", HashSet.of(".kt"), HashSet.of(Language.JAVA.name)),
  // TODO https://youtrack.jetbrains.com/issue/BAZEL-25
  // CPP("cpp", HashSet.of(".C", ".cc", ".cpp", ".CPP", ".c++", ".cp", "cxx", ".h", ".hpp")),
  THRIFT("thrift", HashSet.of(".thrift"));

  private final String name;
  private final Set<String> extensions;
  private final Set<String> allNames;

  Language(String name, Set<String> extensions, Set<String> dependentNames) {
    this.name = name;
    this.extensions = extensions;
    this.allNames = dependentNames.add(name);
  }

  Language(String name, Set<String> extensions) {
    this(name, extensions, HashSet.of());
  }

  public static List<Language> all() {
    return List.of(values());
  }

  public String getName() {
    return name;
  }

  public Set<String> getExtensions() {
    return extensions;
  }

  public Set<String> getAllNames() {
    return allNames;
  }
}
