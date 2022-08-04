package org.jetbrains.bsp.bazel.server.bloop;

import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.jetbrains.bsp.bazel.server.sync.model.Label;

class Naming {
  private Naming() {}

  public static String compilerOutputNameFor(Label label) {
    try {
      var digest = MessageDigest.getInstance("MD5");
      digest.update(label.getValue().getBytes(StandardCharsets.UTF_8));
      return "z_" + BaseEncoding.base16().encode(digest.digest()).substring(0, 12);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String safeName(Label label) {
    var labelName = label.getValue();
    if (labelName.startsWith("//")) {
      labelName = labelName.substring(2);
    }
    return labelName;
  }
}
