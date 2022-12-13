package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;

class RegexRuleTest
{
  private static final RegexRule<String> regexMatchAllRule = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX,"\\d+","0");
  private static final RegexRule<String> regexMatchWholeStringRule = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX,"^\\d+$","0");
  private static final RegexRule<String> regexMatchLastDigitRule = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX,"\\d+(\\d)","1");

  private static final RegexRule<String> regexTranslateStringRuleOne = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX_TRANSLATE,"[0-4]","low");
  private static final RegexRule<String> regexTranslateStringRuleTwo = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX_TRANSLATE,"[5-9]","high");
  private static final RegexRule<String> regexTranslateWholeStringRule = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX_TRANSLATE,"^[0-4]$","low");
  private static final RegexRule<Double> regexTranslateDoubleRule = new RegexRule<>("test_IN","test_OUT",
      DataType.DOUBLE, Rule.Mode.REGEX_TRANSLATE,"^[0-4]$",0.0);

  private static final RegexRule<String> multiRegexRule = new RegexRule<>("test_IN","test_OUT",
      DataType.STRING, Rule.Mode.REGEX_MULTI,"([0-4])\\d([5-9])","1/2");

  @SuppressWarnings("unused")
  static List<Arguments> generateTestSets()
  {
    List<Arguments> arguments = new LinkedList<>();
    arguments.add(Arguments.of(regexMatchAllRule, "100", "100", true, false));
    arguments.add(Arguments.of(regexMatchAllRule, "a100b", "100", true, false));
    arguments.add(Arguments.of(regexMatchLastDigitRule, "100", "0", true, false));
    arguments.add(Arguments.of(regexMatchLastDigitRule, "a100b", "0", true, false));
    arguments.add(Arguments.of(regexMatchWholeStringRule, "100", "100", true, false));
    arguments.add(Arguments.of(regexMatchWholeStringRule, "a100b", "0", false, false));

    arguments.add(Arguments.of(regexTranslateStringRuleOne, "3", "low", true, false));
    arguments.add(Arguments.of(regexTranslateStringRuleOne, "6", "", false, false));
    arguments.add(Arguments.of(regexTranslateStringRuleTwo, "3", "", false, false));
    arguments.add(Arguments.of(regexTranslateStringRuleTwo, "6", "high", true, false));
    arguments.add(Arguments.of(regexTranslateStringRuleOne, "909", "low", true, false));
    arguments.add(Arguments.of(regexTranslateWholeStringRule, "909", "", false, false));
    arguments.add(Arguments.of(regexTranslateWholeStringRule, "0", "low", true, false));
    arguments.add(Arguments.of(regexTranslateDoubleRule, "0", 0.0, true, false));
    arguments.add(Arguments.of(regexTranslateDoubleRule, "9", -1.0, false, false));

    arguments.add(Arguments.of(multiRegexRule, "418", "48", true, false));
    arguments.add(Arguments.of(multiRegexRule, "48", "", false, false));

    return arguments;
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testCanApply(RegexRule<O> rule, String input, O output, boolean canApply, boolean canReverse)
  {
    Assertions.assertEquals(canApply, rule.canApply(input));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testApply(RegexRule<O> rule, String input, O output, boolean canApply, boolean canReverse)
  {
    if(canApply)
    {
      Assertions.assertEquals(output, rule.apply(input).getData());
    }
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testCanReverse(RegexRule<O> rule, String input, O output, boolean canApply, boolean canReverse)
  {
    Assertions.assertEquals(canReverse, rule.canReverse(output));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  <I, O> void testReverse(RegexRule<O> rule, String input, O output, boolean canApply, boolean canReverse)
  {
    if(canReverse)
    {
      Assertions.assertEquals(input, rule.reverse(output).getData());
    }
  }
}