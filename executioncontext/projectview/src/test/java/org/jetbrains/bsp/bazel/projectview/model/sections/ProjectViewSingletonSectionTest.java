package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ProjectViewSingletonSectionTest<V, T extends ProjectViewSingletonSection<V>> {

  public static Stream<Arguments> data() {
    return Stream.of(
        bazelPathSectionArguments(), debuggerAddressSectionArguments(), javaPathSectionArguments());
  }

  private static Arguments bazelPathSectionArguments() {
    return Arguments.of(createSectionConstructor(ProjectViewBazelPathSection::new, Paths::get));
  }

  private static Arguments debuggerAddressSectionArguments() {
    return Arguments.of(
        createSectionConstructor(ProjectViewDebuggerAddressSection::new, HostAndPort::fromString));
  }

  private static Arguments javaPathSectionArguments() {
    return Arguments.of(createSectionConstructor(ProjectViewJavaPathSection::new, Paths::get));
  }

  private static <V, T extends ProjectViewSingletonSection<V>>
      Function<String, T> createSectionConstructor(
          Function<V, T> sectionMapper, Function<String, V> elementMapper) {

    return (rawValue) -> sectionMapper.apply(elementMapper.apply(rawValue));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues(
      Function<String, T> sectionConstructor) {

    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("value");

    // then
    assertThat(section1).isEqualTo(section2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentValues(
      Function<String, T> sectionConstructor) {

    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("another_value");

    // then
    assertThat(section1).isNotEqualTo(section2);
  }
}
