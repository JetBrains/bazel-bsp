package target_with_dependency;

import com.google.common.collect.Lists;
import java_targets.subpackage.JavaLibrary2;

public class JavaBinary {
  public static void main(String[] args) {
    System.out.println(Lists.newArrayList("Hello", "World", JavaLibrary2.value));
  }
}
