package org.jetbrains.bsp.bazel.server.bloop;

import bloop.config.Config;
import bloop.config.ConfigCodecs;
import bloop.config.package$;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BloopProjectWriter {
  private final Path bloopRoot;

  public BloopProjectWriter(Path bloopRoot) {
    this.bloopRoot = bloopRoot;
  }

  private String safeFileName(String projectName) {
    return projectName.replace('/', '.');
  }

  public Path write(Config.Project bloopProject) {
    var configFile = new Config.File(Config.File$.MODULE$.LatestVersion(), bloopProject);
    var outputPath = bloopRoot.resolve(safeFileName(bloopProject.name()) + ".config.json");
    if (outputPath.toFile().exists()) {
      var configString = ConfigCodecs.toStr(configFile);
      var hasher = Hashing.sha256();
      var newHash = hasher.hashString(configString, StandardCharsets.UTF_8);
      try {
        var existingHash =
            com.google.common.io.Files.asByteSource(outputPath.toFile()).hash(hasher);
        if (!newHash.equals(existingHash)) {
          Files.writeString(outputPath, configString, StandardCharsets.UTF_8);
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      package$.MODULE$.write(configFile, outputPath);
    }
    return outputPath;
  }
}
