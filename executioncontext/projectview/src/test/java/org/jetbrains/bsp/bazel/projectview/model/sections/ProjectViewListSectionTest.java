package org.jetbrains.bsp.bazel.projectview.model.sections;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProjectViewListSectionTest<V, T extends ProjectViewListSection<V>> {

  private BiFunction<List<String>, List<String>, T> createSectionConstructor(
      BiFunction<List<V>, List<V>, T> sectionMapper, Function<String, V> elementMapper) {

    return (includedElements, excludedElements) ->
        sectionMapper.apply(
            includedElements.map(elementMapper), excludedElements.map(elementMapper));
  }

  public static Stream<Arguments> data() {
    var buildTargetIdentifierSectionMapper =
        (BiFunction<
                List<BuildTargetIdentifier>,
                List<BuildTargetIdentifier>,
                ProjectViewTargetsSection>)
            ProjectViewTargetsSection::new;
    var buildTargetIdentifierElementMapper =
        (Function<String, BuildTargetIdentifier>)
            (rawElement) -> new BuildTargetIdentifier("//:" + rawElement);
    return Stream.of(
        Arguments.of(buildTargetIdentifierSectionMapper, buildTargetIdentifierElementMapper));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues(
      BiFunction<io.vavr.collection.List<V>, io.vavr.collection.List<V>, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);

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

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentIncludedValues(
      BiFunction<io.vavr.collection.List<V>, io.vavr.collection.List<V>, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);
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

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentExcludedValues(
      BiFunction<io.vavr.collection.List<V>, io.vavr.collection.List<V>, T> sectionMapper,
      Function<String, V> elementMapper) {
    var sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);
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
