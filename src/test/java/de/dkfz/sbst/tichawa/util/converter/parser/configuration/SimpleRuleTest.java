package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

class SimpleRuleTest
{
  private static final SimpleRule<String, String> keepStringRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.STRING, Rule.Mode.KEEP,"","");
  private static final SimpleRule<Integer, Integer> keepIntegerRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.INTEGER, DataType.INTEGER, Rule.Mode.KEEP,0,1);
  private static final SimpleRule<Double, Double> keepDoubleRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.DOUBLE, DataType.DOUBLE, Rule.Mode.KEEP,null,null);
  private static final SimpleRule<String, Integer> keepStringIntegerRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.INTEGER, Rule.Mode.KEEP,"",1);

  private static final SimpleRule<String, String> translateStringRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.STRING, Rule.Mode.TRANSLATE, "one", "two");
  private static final SimpleRule<Integer, Integer> translateIntegerRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.INTEGER, DataType.INTEGER, Rule.Mode.TRANSLATE,1,2);
  private static final SimpleRule<String, Integer> translateStringIntegerRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.INTEGER, Rule.Mode.TRANSLATE,"one",1);

  private static final SimpleRule<String, String> staticStringRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.STRING, Rule.Mode.STATIC,"null","two");
  private static final SimpleRule<Integer, Integer> staticIntegerRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.INTEGER, DataType.INTEGER, Rule.Mode.STATIC,0,2);

  private static final SimpleRule<String, Instant> specialStringInstantRule = new SimpleRule<>("test_IN","test_OUT",
      DataType.STRING, DataType.INSTANT, Rule.Mode.SPECIAL,"NOW",null);

  @SuppressWarnings("unused")
  static List<Arguments> generateTestSets()
  {
    List<Arguments> arguments = new LinkedList<>();
    arguments.add(Arguments.of(keepStringRule, "", "", true, true));
    arguments.add(Arguments.of(keepStringRule, "one", "one", true, true));
    arguments.add(Arguments.of(keepStringRule, "two", "two", true, true));
    arguments.add(Arguments.of(keepStringRule, "three", "three", true, true));
    arguments.add(Arguments.of(keepIntegerRule, 0, 0, true, true));
    arguments.add(Arguments.of(keepIntegerRule, 1, 1, true, true));
    arguments.add(Arguments.of(keepIntegerRule, 2, 2, true, true));
    arguments.add(Arguments.of(keepIntegerRule, 3, 3, true, true));
    arguments.add(Arguments.of(keepIntegerRule, -1, -1, true, true));
    arguments.add(Arguments.of(keepDoubleRule, 0.0, 0.0, true, true));
    arguments.add(Arguments.of(keepDoubleRule, 1.0, 1.0, true, true));
    arguments.add(Arguments.of(keepDoubleRule, 2.0, 2.0, true, true));
    arguments.add(Arguments.of(keepDoubleRule, 3.0, 3.0, true, true));
    arguments.add(Arguments.of(keepDoubleRule, -1.0, -1.0, true, true));
    arguments.add(Arguments.of(keepStringIntegerRule, "zero", 0, false, false));
    arguments.add(Arguments.of(keepStringIntegerRule, "one", 1, false, false));
    arguments.add(Arguments.of(keepStringIntegerRule, "two", 2, false, false));
    arguments.add(Arguments.of(keepStringIntegerRule, "three", 3, false, false));
    arguments.add(Arguments.of(keepStringIntegerRule, "minus one", -1, false, false));

    arguments.add(Arguments.of(translateStringRule, "", "", false, false));
    arguments.add(Arguments.of(translateStringRule, "one", "two", true, true));
    arguments.add(Arguments.of(translateStringRule, "two", "one", false, false));
    arguments.add(Arguments.of(translateStringRule, "three", "three", false, false));
    arguments.add(Arguments.of(translateIntegerRule, 0, 0, false, false));
    arguments.add(Arguments.of(translateIntegerRule, 1, 2, true, true));
    arguments.add(Arguments.of(translateIntegerRule, 2, 1, false, false));
    arguments.add(Arguments.of(translateIntegerRule, 3, 3, false, false));
    arguments.add(Arguments.of(translateIntegerRule, -1, -1, false, false));
    arguments.add(Arguments.of(translateStringIntegerRule, "zero", 0, false, false));
    arguments.add(Arguments.of(translateStringIntegerRule, "one", 1, true, true));
    arguments.add(Arguments.of(translateStringIntegerRule, "two", 2, false, false));
    arguments.add(Arguments.of(translateStringIntegerRule, "three", 3, false, false));
    arguments.add(Arguments.of(translateStringIntegerRule, "minus one", -1, false, false));

    arguments.add(Arguments.of(staticStringRule, "null", "two", true, false));
    arguments.add(Arguments.of(staticStringRule, "", "two", true, false));
    arguments.add(Arguments.of(staticIntegerRule, 0, 2, true, false));
    arguments.add(Arguments.of(staticIntegerRule, 1, 2, true, false));

    return arguments;
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testCanApply(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    Assertions.assertEquals(canApply, rule.canApply(input));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testApply(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    if(canApply)
    {
      Assertions.assertEquals(output, rule.apply(input).getData());
    }
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testApplyLabels(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    if(canApply)
    {
      Assertions.assertEquals(rule.getOutLabel(), rule.apply(input).getRule().getOutLabel());
    }
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testCanReverse(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    Assertions.assertEquals(canReverse, rule.canReverse(output));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testReverse(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    if(canReverse)
    {
      Assertions.assertEquals(input, rule.reverse(output).getData());
    }
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testReverseLabels(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    if(canReverse)
    {
      Assertions.assertEquals(rule.getOutLabel(), rule.reverse(output).getRule().getInLabel());
    }
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testApplyAndReverse(SimpleRule<I, O> rule, I input, O output, boolean canApply, boolean canReverse)
  {
    if(canApply && canReverse)
    {
      Rule.Result<O> result = rule.apply(input);
      Assertions.assertEquals(input, rule.reverse(result.getData()).getData());
    }
  }

  @Test
  void testSpecialStringInstantRule()
  {
    Assertions.assertTrue(specialStringInstantRule.canApply(""));
    Assertions.assertTrue(specialStringInstantRule.canApply("NULL"));
    Assertions.assertTrue(specialStringInstantRule.canApply("TEST"));

    Assertions.assertFalse(specialStringInstantRule.canReverse(Instant.now()));

    Instant slightlyBefore = Instant.now().minus(1, ChronoUnit.MILLIS);
    Instant generatedTime = specialStringInstantRule.apply("").getData();
    Instant slightlyAfter = Instant.now().plus(1, ChronoUnit.MILLIS);

    Assertions.assertTrue(slightlyBefore.isBefore(generatedTime));
    Assertions.assertTrue(slightlyAfter.isAfter(generatedTime));
  }
}