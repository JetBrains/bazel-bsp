package org.jetbrains.bsp.bazel.bazelrunner;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;

public class BazelInfoStorage {

  private static final Logger LOGGER = LogManager.getLogger(BazelInfoStorage.class);
  private final ObjectMapper mapper;
  private final Path path;

  public BazelInfoStorage(BspInfo bspInfo) {
    this(bspInfo.bazelBspDir().resolve("bazel-info-cache.json"));
  }

  public BazelInfoStorage(Path path) {
    this.path = path;
    mapper = createMapper();
  }

  private ObjectMapper createMapper() {
    var mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    return mapper;
  }

  public Option<BazelInfo> load() {
    return Option.when(Files.exists(path), this::read).flatMap(Function.identity());
  }

  private Option<BasicBazelInfo> read() {
    return Try.of(() -> mapper.readValue(path.toFile(), BasicBazelInfo.class)).toOption();
  }

  public void store(BasicBazelInfo bazelInfo) {
    try {
      mapper.writeValue(path.toFile(), bazelInfo);
    } catch (IOException e) {
      LOGGER.error("Could not store bazel info", e);
    }
  }
}
