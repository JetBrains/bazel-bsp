package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import io.vavr.collection.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectViewRawSectionsTest {

  @Test
  public void shouldReturnEmptyForEmptySections() {
    // given
    var sections = List.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertFalse(sectionWithName.isDefined());
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
    assertFalse(sectionWithName.isDefined());
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
    assertEquals(expectedSection, sectionWithName.get());
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
    assertEquals(expectedSection, sectionWithName.get());
  }

  @Test
  public void shouldReturnEmptyListForEmptySections() {
    // given
    var sections = List.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist").toJavaList();

    // then
    assertTrue(sectionsWithName.isEmpty());
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
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist").toJavaList();

    // then
    assertTrue(sectionsWithName.isEmpty());
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
    var sectionsWithName = projectViewRawSections.getAllWithName("name1").toJavaList();

    // then
    var expectedSections =
        List.of(
                new ProjectViewRawSection("name1", "body1.1"),
                new ProjectViewRawSection("name1", "body1.3"),
                new ProjectViewRawSection("name1", "body1.2"))
            .toJavaList();
    assertTrue(CollectionUtils.isEqualCollection(expectedSections, sectionsWithName));
  }
}
