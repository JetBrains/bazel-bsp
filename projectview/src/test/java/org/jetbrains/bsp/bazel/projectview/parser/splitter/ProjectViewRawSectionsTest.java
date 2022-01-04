package org.jetbrains.bsp.bazel.projectview.parser.splitter;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProjectViewRawSectionsTest {

  @Test
  public void shouldReturnEmptyForEmptySections() {
    // given
    List<ProjectViewRawSection> sections = ImmutableList.of();
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    Optional<ProjectViewRawSection> sectionWithName =
        projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertFalse(sectionWithName.isPresent());
  }

  @Test
  public void shouldReturnEmptyIfSectionDoesntExist() {
    // given
    List<ProjectViewRawSection> sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    Optional<ProjectViewRawSection> sectionWithName =
        projectViewRawSections.getLastSectionWithName("doesntexist");

    // then
    assertFalse(sectionWithName.isPresent());
  }

  @Test
  public void shouldReturnOnlySectionWithName() {
    // given
    List<ProjectViewRawSection> sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    Optional<ProjectViewRawSection> sectionWithName =
        projectViewRawSections.getLastSectionWithName("name1");

    // then
    ProjectViewRawSection expectedSection = new ProjectViewRawSection("name1", "body1");
    assertEquals(expectedSection, sectionWithName.get());
  }

  @Test
  public void shouldReturnLastSectionWithName() {
    // given
    List<ProjectViewRawSection> sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"),
            new ProjectViewRawSection("name3", "body3"));
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    Optional<ProjectViewRawSection> sectionWithName =
        projectViewRawSections.getLastSectionWithName("name1");

    // then
    ProjectViewRawSection expectedSection = new ProjectViewRawSection("name1", "body1.3");
    assertEquals(expectedSection, sectionWithName.get());
  }

  @Test
  public void shouldReturnEmptyListForEmptySections() {
    // given
    List<ProjectViewRawSection> sections = ImmutableList.of();
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    List<ProjectViewRawSection> sectionsWithName =
        projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertTrue(sectionsWithName.isEmpty());
  }

  @Test
  public void shouldReturnEmptyListIfSectionDoesntExist() {
    // given
    List<ProjectViewRawSection> sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name3", "body3"));
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    List<ProjectViewRawSection> sectionsWithName =
        projectViewRawSections.getAllWithName("doesntexist");

    // then
    assertTrue(sectionsWithName.isEmpty());
  }

  @Test
  public void shouldReturnAllSectionsWithName() {
    // given
    List<ProjectViewRawSection> sections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name2", "body2"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"),
            new ProjectViewRawSection("name3", "body3"));
    ProjectViewRawSections projectViewRawSections = new ProjectViewRawSections(sections);

    // when
    List<ProjectViewRawSection> sectionsWithName = projectViewRawSections.getAllWithName("name1");

    // then
    List<ProjectViewRawSection> expectedSections =
        ImmutableList.of(
            new ProjectViewRawSection("name1", "body1.1"),
            new ProjectViewRawSection("name1", "body1.2"),
            new ProjectViewRawSection("name1", "body1.3"));
    assertEquals(expectedSections, sectionsWithName);
  }
}
