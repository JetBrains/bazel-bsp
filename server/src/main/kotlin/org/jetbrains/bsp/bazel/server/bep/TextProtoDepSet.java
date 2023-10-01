package org.jetbrains.bsp.bazel.server.bep;

import java.net.URI;
import java.util.Collection;

public class TextProtoDepSet {
  private final Collection<URI> files;
  private final Collection<String> children;

  TextProtoDepSet(Collection<URI> files, Collection<String> children) {
    this.files = files;
    this.children = children;
  }

  public Collection<URI> getFiles() {
    return files;
  }

  public Collection<String> getChildren() {
    return children;
  }
}
