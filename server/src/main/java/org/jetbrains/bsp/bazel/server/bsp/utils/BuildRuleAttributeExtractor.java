package org.jetbrains.bsp.bazel.server.bsp.utils;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.ListUtils;

public final class BuildRuleAttributeExtractor {

  public static List<String> extract(Build.Rule rule, String expectedAttributeName) {
    return extract(rule, ImmutableList.of(expectedAttributeName));
  }

  public static List<String> extract(Build.Rule rule, List<String> expectedAttributesNames) {
    return rule.getAttributeList().stream()
        .filter(attribute -> doesAttributeHaveExpectedName(attribute, expectedAttributesNames))
        .map(BuildRuleAttributeExtractor::getAllStringValues)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private static boolean doesAttributeHaveExpectedName(
      Build.Attribute attribute, List<String> attributesNames) {
    String attributeName = attribute.getName();

    return attributesNames.contains(attributeName) && attribute.getExplicitlySpecified();
  }

  private static List<String> getAllStringValues(Build.Attribute attribute) {
    List<String> stringListValues = attribute.getStringListValueList();
    List<String> stringValue = getStringValueAsListOrEmptyIfNotExists(attribute);

    return ListUtils.concat(stringListValues, stringValue);
  }

  private static List<String> getStringValueAsListOrEmptyIfNotExists(Build.Attribute attribute) {
    if (attribute.hasStringValue()) {
      return ImmutableList.of(attribute.getStringValue());
    }

    return ImmutableList.of();
  }
}
