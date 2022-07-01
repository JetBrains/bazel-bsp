package org.jetbrains.bsp.bazel.server.sync.languages;

import io.vavr.control.Option;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser;

public class JVMLanguagePluginParser {
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w\\.]+)");

  public JVMLanguagePluginParser() {}

  public static Option<Path> calculateJVMSourceRoot(Path source, boolean multipleLines) {
    String sourcePackage = findPackage(source, multipleLines);

    if (sourcePackage == null) {
      return Option.some(SourceRootGuesser.getSourcesRoot(source));
    }
    Path sourcePackagePath = Paths.get(sourcePackage.replace(".", "/"));

    return Option.some(
        Paths.get("/")
            .resolve(
                source.subpath(0, source.getNameCount() - sourcePackagePath.getNameCount() - 1)));
  }

  private static String findPackage(Path source, boolean multipleLines) {
    List<String> packages;
    File sourceFile = new File(String.valueOf(source));
    try {
      BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
      packages = findPackages(reader);
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    if (!packages.isEmpty()) {
      if (multipleLines) {
        return String.join(".", packages);
      } else {
        return packages.get(0);
      }
    }
    return null;
  }

  private static List<String> findPackages(BufferedReader reader) throws IOException {
    List<String> packages = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher = PACKAGE_PATTERN.matcher(line);
      if (matcher.find()) {
        packages.add(matcher.group(1));
      }
    }
    return packages;
  }
}
