package org.jetbrains.bsp.bazel.commons;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ListUtils {

  public static <T> List<T> concat(List<T> list1, List<T> list2) {
    return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
  }
}
