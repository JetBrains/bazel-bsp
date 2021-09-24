package org.jetbrains.bsp.bazel.server.bazel.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class BazelStreamReader {

  public static List<String> drainStream(InputStream stream) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      List<String> list = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        list.add(line.trim());
      }
      return list;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
