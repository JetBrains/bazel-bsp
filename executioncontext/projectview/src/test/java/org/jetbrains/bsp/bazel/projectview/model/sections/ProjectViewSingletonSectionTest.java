package org.jetbrains.bsp.bazel.projectview.model.sections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.net.HostAndPort;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ProjectViewSingletonSectionTest<V, T extends ProjectViewSingletonSection<V>> {

  private Function<String, T> createSectionConstructor(
      Function<V, T> sectionMapper, Function<String, V> elementMapper) {

    return (rawValue) -> sectionMapper.apply(elementMapper.apply(rawValue));
  }

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            (Function<Path, ProjectViewBazelPathSection>) ProjectViewBazelPathSection::new,
            (Function<String, Path>) Paths::get),
        Arguments.of(
            (Function<HostAndPort, ProjectViewDebuggerAddressSection>)
                ProjectViewDebuggerAddressSection::new,
            (Function<String, HostAndPort>) HostAndPort::fromString),
        Arguments.of(
            (Function<Path, ProjectViewJavaPathSection>) ProjectViewJavaPathSection::new,
            (Function<String, Path>) Paths::get));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnTrueForTheSameSectionsWithTheSameValues(
      Function<V, T> sectionMapper, Function<String, V> elementMapper) {
    var sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);

    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("value");

    // then
    assertEquals(section1, section2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void shouldReturnFalseForTheSameSectionsWithDifferentValues(
      Function<V, T> sectionMapper, Function<String, V> elementMapper) {
    var sectionConstructor = createSectionConstructor(sectionMapper, elementMapper);

    // given & when
    var section1 = sectionConstructor.apply("value");
    var section2 = sectionConstructor.apply("another_value");

    // then
    assertNotEquals(section1, section2);
  }
}
