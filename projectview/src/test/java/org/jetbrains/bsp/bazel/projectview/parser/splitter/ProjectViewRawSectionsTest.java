package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import com.google.common.collect.ImmutableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ProjectViewRawSectionsTest {

  @Test
  public void shouldReturnEmptyForEmptySections() {
    // given
    var sections = ImmutableList.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertFalse(sectionWithName.isPresent());
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    var sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionWithName = projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertFalse(sectionWithName.isPresent());
  }

  @Test
  public void shouldReturnOnlySectionWithName() {
    // given
    var sections =
        ImmutableList.of(
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
        ImmutableList.of(
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
    var sections = ImmutableList.<ProjectViewRawSection>of();
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertTrue(sectionsWithName.isEmpty());
  }

  @Test
  public void shouldReturnEmptyListIfSectionDoesntExist() {
    // given
    var sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    var projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    var sectionsWithName = projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertTrue(sectionsWithName.isEmpty());
  }

  @Test
  public void shouldReturnAllSectionsWithName() {
    // given
    var sections =
        ImmutableList.of(
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
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"));
    assertEquals(expectedSections, sectionsWithName);
  }
}
