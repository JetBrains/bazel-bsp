package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.net.HostAndPort;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewSingletonSectionTest<V, T extends ProjectViewSingletonSection<V>> {

  private final Function<String, T> sectionConstructor;

  public ProjectViewSingletonSectionTest(
      Function<V, T> sectionMapper, Function<String, V> elementMapper) {
    this.sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);
  }

  private Function<String, T> createSectionConstructor(
      Function<V, T> sectionMapper, Function<String, V> elementMapper) {

    return (rawValue) -> sectionMapper.apply(elementMapper.apply(rawValue));
  }

  @Parameters(
      name = "{index}: .equals() on a singleton section for {0} and {1} as another type section")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            (Function<Path, ProjectViewBazelPathSection>) ProjectViewBazelPathSection::new,
            (Function<String, Path>) Paths::get
          },
          {
            (Function<HostAndPort, ProjectViewDebuggerAddressSection>)
                ProjectViewDebuggerAddressSection::new,
            (Function<String, HostAndPort>) HostAndPort::fromString
          },
          {
            (Function<Path, ProjectViewJavaPathSection>) ProjectViewJavaPathSection::new,
            (Function<String, Path>) Paths::get
          }
        });
  }

  @Test
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues() {
    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("value");

    // then
    assertEquals(section1, section2);
  }

  @Test
  public void shouldReturnFalseForTheSameSectionsWithDifferentValues() {
    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("another_value");

    // then
    assertNotEquals(section1, section2);
  }
}
