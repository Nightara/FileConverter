package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Rule.*;
import org.junit.jupiter.api.*;

import java.util.*;

@SuppressWarnings("unchecked")
class SimpleStringParserTest
{
  private static final List<Rule<Object, Object>> RULES = new LinkedList<>();
  static
  {
    Rule<?, ?> firstRule = new SimpleRule<>("in_one","out_two", DataType.STRING, DataType.STRING, Mode.TRANSLATE,"one","three");
    RULES.add((Rule<Object, Object>) firstRule);
    Rule<?, ?> secondRule = new SimpleRule<>("in_one","out_two", DataType.STRING, DataType.STRING, Mode.TRANSLATE,"two", "one");
    RULES.add((Rule<Object, Object>) secondRule);
    Rule<?, ?> thirdRule = new SimpleRule<>("in_two","out_one", DataType.INTEGER, DataType.INTEGER, Mode.KEEP,0,0);
    RULES.add((Rule<Object, Object>) thirdRule);
  }
  private static final Configuration CONFIG = new Configuration(RULES);
  private static final SimpleStringParser PARSER = new SimpleStringParser("Custom",null, CONFIG,
      "\t","\n","in_one", "in_two");

  @Test
  void testTranslate()
  {
    Assertions.assertEquals("three\n2", PARSER.translate("one\t2"));
    Assertions.assertEquals("one\n0", PARSER.translate("two\t0"));
  }

  @Test
  void testParse()
  {
    Map<String, Result<Object>> results = PARSER.parse("one\t2");

    Assertions.assertEquals(2, results.size());
    Assertions.assertTrue(results.containsKey("out_one"));
    Assertions.assertTrue(results.containsKey("out_two"));
    Assertions.assertInstanceOf(Integer.class, results.get("out_one").data());
    Assertions.assertInstanceOf(String.class, results.get("out_two").data());
  }

  @Test
  void testEncode()
  {
    Map<String, Result<Object>> results = PARSER.parse("one\t2");
    String[] output = PARSER.encode(results).split(PARSER.getOutSeparator());

    Assertions.assertEquals(CONFIG.getOutLabels().size(), output.length);
    for(int x = 0; x < output.length; x++)
    {
      Assertions.assertEquals(results.get(CONFIG.getOutLabels().get(x)).data().toString(), output[x]);
    }
  }
}