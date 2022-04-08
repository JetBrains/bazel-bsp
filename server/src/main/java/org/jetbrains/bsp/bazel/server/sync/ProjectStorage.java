package org.jetbrains.bsp.bazel.server.sync;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

public class ProjectStorage {
  private final ObjectMapper mapper;
  private final Path path;
  private final BspClientLogger logger;

  public ProjectStorage(BspInfo bspInfo, BspClientLogger logger) {
    this(bspInfo.bazelBspDir().resolve("project-cache.json"), logger);
  }

  public ProjectStorage(Path path, BspClientLogger logger) {
    this.path = path;
    this.logger = logger;
    mapper = createMapper();
  }

  private ObjectMapper createMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new VavrModule());
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    return mapper;
  }

  public Option<Project> load() {
    return Option.when(Files.exists(path), this::read);
  }

  private Project read() {
    return logger.timed(
        "Loading project from local cache",
        () -> {
          try {
            return mapper.readValue(path.toFile(), Project.class);
          } catch (IOException e) {
            // TODO figure out why this error is otherwise not propagated to bsp client
            logger.error(e.toString());
            throw new RuntimeException(e);
          }
        });
  }

  public void store(Project project) {
    logger.timed(
        "Saving project to local cache",
        () -> {
          try {
            mapper.writeValue(path.toFile(), project);
          } catch (IOException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
          }
        });
  }
}
