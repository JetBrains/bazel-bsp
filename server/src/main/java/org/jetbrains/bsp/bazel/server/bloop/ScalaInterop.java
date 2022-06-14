package org.jetbrains.bsp.bazel.server.bloop;

import io.vavr.control.Option;
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

  public static <T> scala.collection.immutable.List<T> toList(Iterable<T> input) {
    return CollectionConverters.asScala(input).toList();
  }

  public static <T> scala.collection.immutable.List<T> toList(Iterator<T> input) {
    return CollectionConverters.asScala(input).toList();
  }
}
