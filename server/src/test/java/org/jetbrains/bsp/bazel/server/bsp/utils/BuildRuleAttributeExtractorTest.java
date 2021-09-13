package org.jetbrains.bsp.bazel.server.bsp.utils;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildRuleAttributeExtractorTest {

  private static String attribute1Name;
  private static List<String> attribute1Values;

  private static String attribute2Name;
  private static List<String> attribute2Values;

  private static String attribute3Name;
  private static List<String> attribute3Values;

  private static Build.Rule rule;

  @BeforeClass
  public static void beforeAll() {
    // given
    attribute1Name = "attribute1";
    attribute1Values = ImmutableList.of("attribute1-val1", "attribute1-val2");

    attribute2Name = "attribute2";
    attribute2Values = ImmutableList.of("attribute2-val1");

    attribute3Name = "attribute3";
    attribute3Values = ImmutableList.of();

    Build.Attribute attribute1 =
        Build.Attribute.newBuilder()
            .setName(attribute1Name)
            .addAllStringListValue(attribute1Values)
            .buildPartial();

    Build.Attribute attribute2 =
        Build.Attribute.newBuilder()
            .setName(attribute2Name)
            .addAllStringListValue(attribute2Values)
            .buildPartial();

    Build.Attribute attribute3 =
        Build.Attribute.newBuilder()
            .setName(attribute3Name)
            .addAllStringListValue(attribute3Values)
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
  public void shouldReturnAttributeValues() {
    // when
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attribute1Name);

    // then
    assertThat(values).containsExactlyInAnyOrderElementsOf(attribute1Values);
  }

  @Test
  public void shouldReturnNoValuesForAttributeWithoutValues() {
    // when
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attribute3Name);

    // then
    assertThat(values).isEqualTo(attribute3Values);
  }

  @Test
  public void shouldReturnAttributesValues() {
    // when
    List<String> attributes = ImmutableList.of(attribute1Name, attribute2Name, attribute3Name);
    List<String> values = BuildRuleAttributeExtractor.extract(rule, attributes);

    // then
    List<String> expectedValues = ListUtils.concat(attribute1Values, attribute2Values);
    assertThat(values).containsExactlyInAnyOrderElementsOf(expectedValues);
  }
}
