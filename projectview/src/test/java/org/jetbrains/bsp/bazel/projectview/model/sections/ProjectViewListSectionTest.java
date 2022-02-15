package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewListSectionTest<T extends ProjectViewListSection> {

  private final BiFunction<List<String>, List<String>, T> sectionConstructor;

  public ProjectViewListSectionTest(BiFunction<List<String>, List<String>, T> sectionConstructor) {
    this.sectionConstructor = sectionConstructor;
  }

  @Parameters(name = "{index}: .equals() on a list section for {0}")
  public static Collection<Object[]> data() {
    return List.of(
        new Object[][] {
          {
            (BiFunction<List<String>, List<String>, ProjectViewListSection>)
                ProjectViewTargetsSection::new
          }
        });
  }

  @Test
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues() {
    // given & when
    var section1 =
        sectionConstructor.apply(
            List.of("included_value1", "included_value2"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2"));
    var section2 =
        sectionConstructor.apply(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value2", "excluded_value1"));

    // then
    assertEquals(section1, section2);
  }

  @Test
  public void shouldReturnFalseForTheSameSectionsWithDifferentIncludedValues() {
    // given & when
    var section1 =
        sectionConstructor.apply(
            List.of("included_value1", "included_value3"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2"));
    var section2 =
        sectionConstructor.apply(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value2", "excluded_value1"));

    // then
    assertNotEquals(section1, section2);
  }

  @Test
  public void shouldReturnFalseForTheSameSectionsWithDifferentExcludedValues() {
    // given & when
    var section1 =
        sectionConstructor.apply(
            List.of("included_value1", "included_value2"),
            List.of("excluded_value1", "excluded_value3", "excluded_value2"));
    var section2 =
        sectionConstructor.apply(
            List.of("included_value2", "included_value1"),
            List.of("excluded_value3", "excluded_value5", "excluded_value1"));

    // then
    assertNotEquals(section1, section2);
  }
}
