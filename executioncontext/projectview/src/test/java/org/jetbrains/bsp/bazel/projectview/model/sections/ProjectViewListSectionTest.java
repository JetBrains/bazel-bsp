package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectViewListSectionTest<V, T extends ProjectViewListSection<V>> {

  private final Function<List<String>, T> sectionConstructor;

  public ProjectViewListSectionTest(
      Function<List<V>, T> sectionMapper, Function<String, V> elementMapper) {
    this.sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);
  }

  private Function<List<String>, T> createSectionConstructor(
      Function<List<V>, T> sectionMapper, Function<String, V> elementMapper) {
    return elements -> sectionMapper.apply(elements.map(elementMapper));
  }

  @Parameters(name = "{index}: .equals() on a list section for {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            (Function<List<String>, ProjectViewBuildFlagsSection>)
                ProjectViewBuildFlagsSection::new,
            (Function<String, String>) (rawElement) -> "--flag_" + rawElement + "=dummy_value",
          }
        });
  }

  @Test
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues() {
    // given & when
    var section1 = sectionConstructor.apply(List.of("value1", "value2"));
    var section2 = sectionConstructor.apply(List.of("value1", "value2"));

    // then
    assertThat(section1).isEqualTo(section2);
  }

  @Test
  public void shouldReturnFalseForTheSameSectionsWithDifferentValues() {
    // given & when
    var section1 = sectionConstructor.apply(List.of("value1", "value3"));
    var section2 = sectionConstructor.apply(List.of("value2", "value1"));

    // then
    assertThat(section1).isNotEqualTo(section2);
  }
}
