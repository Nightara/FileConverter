package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import org.junit.jupiter.api.*;

import java.util.*;

@SuppressWarnings("unchecked")
class FilterRuleTest
{
  private static final List<Rule<Object, Object>> RULES = new LinkedList<>();
  private static final Rule<?, ?> FIRST_RULE = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"one","three");
  private static final Rule<?, ?> SECOND_RULE = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"two", "one");
  private static final Rule<?, ?> THIRD_RULE = new SimpleRule<>("in_two","out_one", Configuration.DataType.INTEGER, Configuration.DataType.INTEGER, Rule.Mode.KEEP,0,0);

  static
  {
    RULES.add((Rule<Object, Object>) FIRST_RULE);
    RULES.add((Rule<Object, Object>) SECOND_RULE);
    RULES.add((Rule<Object, Object>) THIRD_RULE);
    RULES.add((Rule<Object, Object>) new FilterRule<>(FIRST_RULE));
  }
  private static final Configuration CONFIG = new Configuration(RULES);
  private static final SimpleStringParser PARSER = new SimpleStringParser("Custom",null, CONFIG,
      "\t","\n","in_one", "in_two");

  @Test
  void testParse()
  {
    Map<String, Rule.Result<Object>> results_filtered = PARSER.parse("one\t2");
    Assertions.assertEquals(0, results_filtered.size());

    Map<String, Rule.Result<Object>> results_unfiltered = PARSER.parse("two\t2");
    Assertions.assertEquals(2, results_unfiltered.size());
  }
}