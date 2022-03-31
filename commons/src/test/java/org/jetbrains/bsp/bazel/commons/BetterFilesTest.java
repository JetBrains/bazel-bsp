package org.jetbrains.bsp.bazel.commons;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class BetterFilesTest {

  @Test
  public void shouldReturnFailureForNotExistingFile() {
    // given
    var filePath = Paths.get("file/doesnt/exist");

    // when
    var fileContent = BetterFiles.tryReadFileContent(filePath);

    // then
    assertThat(fileContent.isFailure()).isTrue();
  }

  @Test
  public void shouldParseExistingFile() throws IOException {
    // given
    var file = File.createTempFile("test", "file");
    file.deleteOnExit();
    var filePath = file.toPath();

    var fileWriter = new FileWriter(file);

    fileWriter.write("test content");
    fileWriter.close();

    // when
    var fileContent = BetterFiles.tryReadFileContent(filePath);

    // then
    assertThat(fileContent.isSuccess()).isTrue();
    assertThat(fileContent.get()).isEqualTo("test content");
  }
}
