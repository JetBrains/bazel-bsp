package org.jetbrains.bsp.bazel.server.bloop;

import com.google.common.collect.Lists;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import scala.collection.immutable.List$;
import scala.jdk.javaapi.CollectionConverters;

class ScalaInterop {
  private ScalaInterop() {}

  public static <T> scala.collection.immutable.List<T> emptyList() {
    return List$.MODULE$.empty();
  }

  public static <T> scala.Option<T> toOption(Option<T> opt) {
    return opt.fold(scala.Option::empty, scala.Some::apply);
  }

  public static scala.collection.immutable.List<Path> toList(Path input) {
    return toList(Collections.singletonList(input));
  }

  public static <T> scala.collection.immutable.List<T> toList(Iterable<T> input) {
    return CollectionConverters.asScala(input).toList();
  }

  public static <T> scala.collection.immutable.List<T> toList(Iterator<T> input) {
    return CollectionConverters.asScala(input).toList();
  }

  public static <T> scala.collection.immutable.List<T> toList(T first, T... rest) {
    var lst = Lists.<T>newArrayList();
    lst.add(first);
    lst.addAll(Arrays.asList(rest));
    return toList(lst);
  }
}
