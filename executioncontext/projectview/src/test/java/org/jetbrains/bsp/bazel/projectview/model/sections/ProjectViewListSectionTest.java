package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ProjectViewListSectionTest<V, T extends ProjectViewListSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(targetsSectionArguments());
  }

  private static Arguments targetsSectionArguments() {
    var sectionConstructor =
        createSectionConstructor(
                ProjectViewBuildFlagsSection::new,
                (Function<String, String>) (seed) -> "--flag_" + seed + "=dummy_value");

    return Arguments.of(sectionConstructor);
  }

  private static <V, T extends ProjectViewListSection<V>>
      Function<List<String>, T> createSectionConstructor(
          Function<List<V>, T> sectionMapper, Function<String, V> elementMapper) {

    return (elements) ->
        sectionMapper.apply(
                elements.map(elementMapper));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues(
      Function<List<String>, T> sectionConstructor) {
    // given & when
    var section1 = sectionConstructor.apply(List.of("value1", "value2"));
    var section2 = sectionConstructor.apply(List.of("value1", "value2"));

    // then
    assertThat(section1).isEqualTo(section2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentValues(Function<List<String>, T> sectionConstructor) {
    // given & when
    var section1 = sectionConstructor.apply(List.of("value1", "value3"));
    var section2 = sectionConstructor.apply(List.of("value2", "value1"));

    // then
    assertThat(section1).isNotEqualTo(section2);
  }
}
