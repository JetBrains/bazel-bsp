package org.jetbrains.bsp.bazel.server.sync.languages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface LanguageData {}
