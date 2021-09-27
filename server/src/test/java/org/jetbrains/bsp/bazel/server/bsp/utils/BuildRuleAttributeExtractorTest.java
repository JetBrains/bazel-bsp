package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuildRuleAttributeExtractorTest {

  private static String attribute1WithListValuesName;
  private static List<String> attribute1Values;

  private static String attribute2WithSingleValueName;
  private static String attribute2Value;

  private static String attribute3NotExplicitlySpecifiedName;

  private static Build.Rule rule;

  @BeforeClass
  public static void beforeAll() {
    // given
    attribute1WithListValuesName = "attribute1";
    attribute1Values = ImmutableList.of("attribute1-val1", "attribute1-val2");

    attribute2WithSingleValueName = "attribute2";
    attribute2Value = "attribute2-val1";

    attribute3NotExplicitlySpecifiedName = "attribute3";

    Build.Attribute attribute1 =
        Build.Attribute.newBuilder()
            .setName(attribute1WithListValuesName)
            .addAllStringListValue(attribute1Values)
            .setExplicitlySpecified(true)
            .buildPartial();

    Build.Attribute attribute2 =
        Build.Attribute.newBuilder()
            .setName(attribute2WithSingleValueName)
            .setStringValue(attribute2Value)
            .setExplicitlySpecified(true)
            .buildPartial();

    Build.Attribute attribute3 =
        Build.Attribute.newBuilder()
            .setName(attribute3NotExplicitlySpecifiedName)
            .setStringValue("not-explicitly-specified-value")
            .buildPartial();

    rule =
        Build.Rule.newBuilder()
            .addAllAttribute(ImmutableList.of(attribute1, attribute2, attribute3))
            .buildPartial();
  }

  @Test
  public void shouldReturnEmptyListForNotExistingAttribute() {
    // when
    String notExistingAttributeName = "not-existing-attribute";

    List<String> values = BuildRuleAttributeExtractor.extract(rule, notExistingAttributeName);

    // then
    assertThat(values).isEmpty();
  }

  @Test
  public void shouldReturnEmptyListForNotExistingAttributes() {
    // when
    String notExistingAttribute1Name = "not-existing-attribute-1";
    String notExistingAttribute2Name = "not-existing-attribute-2";
    List<String> notExistingAttributes =
        ImmutableList.of(notExistingAttribute1Name, notExistingAttribute2Name);

    List<String> values = BuildRuleAttributeExtractor.extract(rule, notExistingAttributes);

    // then
    assertThat(values).isEmpty();
  }

  @Test
  public void shouldReturnEmptyListForNotExplicitlySpecifiedAttribute() {
    // when
    List<String> values =
        BuildRuleAttributeExtractor.extract(rule, attribute3NotExplicitlySpecifiedName);

    // then
    assertThat(values).isEmpty();
  }

  @Test
  public void shouldReturnAttributeListValues() {
    // when
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attribute1WithListValuesName);

    // then
    assertThat(values).containsExactlyInAnyOrderElementsOf(attribute1Values);
  }

  @Test
  public void shouldReturnAttributeSingleValue() {
    // when
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attribute2WithSingleValueName);

    // then
    assertThat(values).containsExactlyInAnyOrder(attribute2Value);
  }

  @Test
  public void shouldReturnNoValuesForAttributeWithoutValues() {
    // when
    List<String> values =
        BuildRuleAttributeExtractor.extract(rule, attribute3NotExplicitlySpecifiedName);

    // then
    assertThat(values).isEmpty();
  }

  @Test
  public void shouldReturnAttributesValuesOnlyForExplicitlySpecifiedAttributes() {
    // when
    List<String> attributes =
        ImmutableList.of(
            attribute1WithListValuesName,
            attribute2WithSingleValueName,
            attribute3NotExplicitlySpecifiedName);
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attributes);

    // then
    List<String> expectedValues =
        ListUtils.concat(attribute1Values, ImmutableList.of(attribute2Value));
    assertThat(values).containsExactlyInAnyOrderElementsOf(expectedValues);
  }
}
