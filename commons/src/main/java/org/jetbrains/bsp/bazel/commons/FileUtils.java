package org.jetbrains.bsp.bazel.commons;

import com.google.common.io.CharSource;
import com.google.common.io.Files;
import io.vavr.control.Try;

import java.nio.charset.Charset;
import java.nio.file.Path;

public final class FileUtils {

  public static String readFileContentOrEmpty(Path filePath) {
    return Try.success(filePath)
        .map(Path::toFile)
        .map(file -> Files.asCharSource(file, Charset.defaultCharset()))
        .mapTry(CharSource::read)
        .getOrElse("");
  }
}
