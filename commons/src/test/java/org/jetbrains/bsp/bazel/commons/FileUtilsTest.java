package org.jetbrains.bsp.bazel.commons;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class FileUtilsTest {

  @Test
  public void shouldReturnEmptyStringParseFile() {
    // given
    Path filePath = Paths.get("file/doesnt/exist");

    // when
    String fileContent = FileUtils.readFileContentOrEmpty(filePath);

    // then
    assertEquals("", fileContent);
  }

  @Test
  public void shouldParseFile() throws IOException {
    // given
    File file = File.createTempFile("test", "file");
    file.deleteOnExit();
    Path filePath = file.toPath();

    FileWriter fileWriter = new FileWriter(file);

    fileWriter.write("test content");
    fileWriter.close();

    // when
    String fileContent = FileUtils.readFileContentOrEmpty(filePath);

    // then
    assertEquals("test content", fileContent);
  }
}
