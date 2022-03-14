package org.jetbrains.bsp.bazel.server.sync.languages.java;

import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;

public class Jdk {
  private final String version;
  private final Option<URI> javaHome;

  public Jdk(String version, Option<URI> javaHome) {
    this.version = version;
    this.javaHome = javaHome;
  }

  public String javaVersion() {
    return version;
  }

  public Option<URI> javaHome() {
    return javaHome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Jdk jdk = (Jdk) o;
    return version.equals(jdk.version) && javaHome.equals(jdk.javaHome);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, javaHome);
  }
}
