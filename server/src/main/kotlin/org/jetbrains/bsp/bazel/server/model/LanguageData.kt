package org.jetbrains.bsp.bazel.server.model

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
interface LanguageData 
