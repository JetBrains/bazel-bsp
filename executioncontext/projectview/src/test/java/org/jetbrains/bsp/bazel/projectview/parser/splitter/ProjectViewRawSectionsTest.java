package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class ProjectViewRawSectionsTest {

  @Test
  public void shouldReturnEmptyForEmptySections() {
    // given
    var sections = List.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertThat(sectionWithName).isEmpty();
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    var sections =
        List.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertThat(sectionWithName).isEmpty();
  }

  @Test
  public void shouldReturnOnlySectionWithName() {
    // given
    var sections =
        List.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("name1");

    // then
    var expectedSection = new ProjectViewRawSection("name1", "body1");
    assertThat(sectionWithName).containsExactly(expectedSection);
  }

  @Test
  public void shouldReturnLastSectionWithName() {
    // given
    var sections =
        List.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("name1");

    // then
    var expectedSection = new ProjectViewRawSection("name1", "body1.3");
    assertThat(sectionWithName).containsExactly(expectedSection);
  }

  @Test
  public void shouldReturnEmptyListForEmptySections() {
    // given
    var sections = List.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertThat(sectionsWithName).isEmpty();
  }

  @Test
  public void shouldReturnEmptyListIfSectionDoesntExist() {
    // given
    var sections =
        List.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertThat(sectionsWithName).isEmpty();
  }

  @Test
  public void shouldReturnAllSectionsWithName() {
    // given
    var sections =
        List.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("name1");

    // then
    var expectedSections =
        List.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name1", "body1.3"),
            new ProjectViewRawSection("name1", "body1.2"));
    assertThat(sectionsWithName).containsExactlyInAnyOrderElementsOf(expectedSections);
  }
}
