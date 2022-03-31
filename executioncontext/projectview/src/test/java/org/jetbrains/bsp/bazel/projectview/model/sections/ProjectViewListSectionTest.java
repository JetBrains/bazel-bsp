package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import java.util.function.BiFunction;
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
            ProjectViewTargetsSection::new,
            (rawElement) -> new BuildTargetIdentifier("//:" + rawElement));

    return Arguments.of(sectionConstructor);
  }

  private static <V, T extends ProjectViewListSection<V>>
      BiFunction<List<String>, List<String>, T> createSectionConstructor(
          BiFunction<List<V>, List<V>, T> sectionMapper, Function<String, V> elementMapper) {

    return (includedElements, excludedElements) ->
        sectionMapper.apply(
            includedElements.map(elementMapper), excludedElements.map(elementMapper));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues(
      BiFunction<List<String>, List<String>, T> sectionConstructor) {
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
    assertThat(section1).isEqualTo(section2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentIncludedValues(
      BiFunction<List<String>, List<String>, T> sectionConstructor) {
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
    assertThat(section1).isNotEqualTo(section2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentExcludedValues(
      BiFunction<List<String>, List<String>, T> sectionConstructor) {

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
    assertThat(section1).isNotEqualTo(section2);
  }
}
