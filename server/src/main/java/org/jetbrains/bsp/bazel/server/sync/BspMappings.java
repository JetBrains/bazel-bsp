package org.jetbrains.bsp.bazel.server.sync;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.BuildTargetTag;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.util.List;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class BspMappings {

  public static BuildTargetIdentifier toBspId(Label label) {
    return new BuildTargetIdentifier(label.getValue());
  }

  public static BuildTargetIdentifier toBspId(Module module) {
    return new BuildTargetIdentifier(module.label().getValue());
  }

  public static Option<String> toBspTag(Tag tag) {
    switch (tag) {
      case APPLICATION:
        return Option.some(BuildTargetTag.APPLICATION);
      case TEST:
        return Option.some(BuildTargetTag.TEST);
      case LIBRARY:
        return Option.some(BuildTargetTag.LIBRARY);
      case NO_IDE:
        return Option.some(BuildTargetTag.NO_IDE);
      case NO_BUILD:
        return Option.none();
      default:
        throw new RuntimeException("Unexpected tag: " + tag);
    }
  }

  public static String toBspUri(URI uri) {
    return uri.toString();
  }

  public static String toBspUri(BuildTargetIdentifier uri) {
    return uri.toString();
  }

  public static Set<Module> getModules(Project project, List<BuildTargetIdentifier> targets) {
    return toLabels(targets).flatMap(project::findModule);
  }

  public static URI toUri(TextDocumentIdentifier textDocument) {
    return URI.create(textDocument.getUri());
  }

  public static Set<Label> toLabels(java.util.List<BuildTargetIdentifier> targets) {
    return HashSet.ofAll(targets).map(BuildTargetIdentifier::getUri).map(Label::from);
  }
}
