package org.jetbrains.bsp.bazel.server.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.java.Jdk;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaSdk;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;
import org.jetbrains.bsp.bazel.utils.dope.DopeTemp;
import org.junit.jupiter.api.Test;

public class ProjectStorageTest {

  @Test
  public void shouldStoreAndLoadProject() throws IOException {
    var path = DopeTemp.INSTANCE.createTempPath("project-cache-test.json");
    Files.deleteIfExists(path);
    var storage = new FileProjectStorage(path, new BspClientLogger());

    var empty = storage.load();
    assertThat(empty).isEmpty();

    var project =
        new Project(
            URI.create("file:///root"),
            List.of(
                new Module(
                    Label.from("//project:project"),
                    false,
                    List.of(Label.from("//project:dep")),
                    HashSet.of(Language.JAVA),
                    HashSet.of(Tag.LIBRARY),
                    URI.create("file:///root/project"),
                    new SourceSet(
                        HashSet.of(URI.create("file:///root/project/Lib.java")),
                        HashSet.of(URI.create("file:///root/project/"))),
                    HashSet.empty(),
                    HashSet.empty(),
                    Option.some(
                        new ScalaModule(
                            new ScalaSdk("org.scala", "2.12.3", "2.12", List.empty()),
                            List.empty(),
                            Option.some(
                                new JavaModule(
                                    new Jdk("8", Option.none()),
                                    Option.none(),
                                    List.empty(),
                                    List.empty(),
                                    URI.create("file:///tmp/out"),
                                    List.empty(),
                                    Option.none(),
                                    List.empty(),
                                    List.empty(),
                                    List.empty(),
                                    List.empty(),
                                    List.empty())))))),
            HashMap.of(URI.create("file:///root/project/Lib.java"), Label.from("file:///root")));

    storage.store(project);
    var loaded = storage.load();
    loaded.forEach(actual -> assertThat(actual).isEqualTo(project));
    loaded.onEmpty(() -> fail("Project not loaded"));
  }
}
