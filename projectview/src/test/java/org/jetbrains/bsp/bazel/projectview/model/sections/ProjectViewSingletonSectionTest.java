package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewSingletonSectionTest<T extends ProjectViewSingletonSection> {

  private final Function<String, T> sectionConstructor;
  private final Function<String, T> anotherTypeSectionConstructor;

  public ProjectViewSingletonSectionTest(
      Function<String, T> sectionConstructor, Function<String, T> anotherTypeSectionConstructor) {
    this.sectionConstructor = sectionConstructor;
    this.anotherTypeSectionConstructor = anotherTypeSectionConstructor;
  }

  @Parameters(
      name = "{index}: .equals() on a singleton section for {0} and {1} as another type section")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            (Function<String, ProjectViewSingletonSection>) ProjectViewBazelPathSection::new,
            (Function<String, ProjectViewSingletonSection>) ProjectViewDebuggerAddressSection::new
          },
          {
            (Function<String, ProjectViewSingletonSection>) ProjectViewDebuggerAddressSection::new,
            (Function<String, ProjectViewSingletonSection>) ProjectViewJavaPathSection::new
          },
          {
            (Function<String, ProjectViewSingletonSection>) ProjectViewJavaPathSection::new,
            (Function<String, ProjectViewSingletonSection>) ProjectViewBazelPathSection::new
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

  @Test
  public void shouldReturnFalseForDifferentTypes() {
    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = anotherTypeSectionConstructor.apply("value");

    // then
    assertNotEquals(section1, section2);
  }
}
