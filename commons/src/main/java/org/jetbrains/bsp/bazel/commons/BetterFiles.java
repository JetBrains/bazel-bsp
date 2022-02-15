package org.jetbrains.bsp.bazel.commons;

import com.google.common.io.CharSource;
import com.google.common.io.Files;
import io.vavr.control.Try;
import java.nio.charset.Charset;
import java.nio.file.Path;

public final class BetterFiles {

  public static Try<String> tryReadFileContent(Path filePath) {
    return Try.success(filePath)
        .map(Path::toFile)
        .map(file -> Files.asCharSource(file, Charset.defaultCharset()))
        .mapTry(CharSource::read);
  }
}
