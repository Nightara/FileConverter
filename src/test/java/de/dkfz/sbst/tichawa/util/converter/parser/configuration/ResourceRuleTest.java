package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

class ResourceRuleTest
{
  private static final ResourceRule<String> stringResourceRule = new ResourceRule<>("test_IN","test_OUT",
      DataType.STRING);
  private static final ResourceRule<Integer> intResourceRule = new ResourceRule<>("test_IN","test_OUT",
      DataType.INTEGER);

  private static final Configuration emptyConfig = new Configuration(List.of(), Map.of());
  private static final Configuration properConfig = new Configuration(List.of(),
      Map.of("key1","val1","key2",2));
  private static final Configuration faultyConfig = new Configuration(List.of(),
      Map.of("key1",1,"key2","val2"));

  static List<Arguments> generateTestSets()
  {
    List<Arguments> arguments = new LinkedList<>();
    arguments.add(Arguments.of(stringResourceRule, emptyConfig, "key1", null, false));
    arguments.add(Arguments.of(stringResourceRule, emptyConfig, "key2", null, false));
    arguments.add(Arguments.of(intResourceRule, emptyConfig, "key1", null, false));
    arguments.add(Arguments.of(intResourceRule, emptyConfig, "key2", null, false));

    arguments.add(Arguments.of(stringResourceRule, properConfig, "key1", "val1", true));
    arguments.add(Arguments.of(stringResourceRule, properConfig, "key2", null, false));
    arguments.add(Arguments.of(stringResourceRule, properConfig, "key3", null, false));
    arguments.add(Arguments.of(intResourceRule, properConfig, "key1", null, false));
    arguments.add(Arguments.of(intResourceRule, properConfig, "key2", 2, true));
    arguments.add(Arguments.of(intResourceRule, properConfig, "key3", null, false));

    arguments.add(Arguments.of(stringResourceRule, faultyConfig, "key1", null, false));
    arguments.add(Arguments.of(stringResourceRule, faultyConfig, "key2", "val2", true));
    arguments.add(Arguments.of(intResourceRule, faultyConfig, "key1", 1, true));
    arguments.add(Arguments.of(intResourceRule, faultyConfig, "key2", null, false));

    return arguments;
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testCanApply(ResourceRule<O> rule, Configuration config, String input, O output, boolean canApply)
  {
    rule.setConfig(null);
    Assertions.assertFalse(rule.canApply(input));
    rule.setConfig(config);
    Assertions.assertEquals(canApply, rule.canApply(input));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testApply(ResourceRule<O> rule, Configuration config, String input, O output, boolean canApply)
  {
    rule.setConfig(config);
    Assumptions.assumingThat(canApply, () -> Assertions.assertEquals(output, rule.apply(input).data()));
  }
}
