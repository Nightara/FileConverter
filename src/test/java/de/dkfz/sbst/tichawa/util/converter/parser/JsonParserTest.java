package de.dkfz.sbst.tichawa.util.converter.parser;

import com.google.gson.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Rule.*;
import org.junit.jupiter.api.*;

import java.util.*;

@SuppressWarnings("unchecked")
class JsonParserTest
{
  private static final Gson GSON = new GsonBuilder().create();
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
  private static final Configuration CONFIG = new Configuration(RULES, new HashMap<>());
  private static final JsonParser PARSER = new JsonParser("Custom",null, CONFIG,
      "in_one", "in_two");

  @Test
  void testTranslate()
  {
    Assertions.assertEquals("{\"out_two\":\"three\",\"out_one\":2}", PARSER.translate("{\"in_one\":\"one\",\"in_two\":2}"));
    Assertions.assertEquals("{\"out_two\":\"one\",\"out_one\":0}", PARSER.translate("{\"in_one\":\"two\",\"in_two\":0}"));
  }

  @Test
  void testParse()
  {
    Map<String, Rule.Result<Object>> results = PARSER.parse("{\"in_one\":\"one\",\"in_two\":2}");

    Assertions.assertEquals(2, results.size());
    Assertions.assertTrue(results.containsKey("out_one"));
    Assertions.assertTrue(results.containsKey("out_two"));
    Assertions.assertInstanceOf(Number.class, results.get("out_one").data());
    Assertions.assertInstanceOf(String.class, results.get("out_two").data());
  }

  @Test
  void testEncode()
  {
    Parser.ParsedLine results = PARSER.parse("{\"in_one\":\"one\",\"in_two\":2}");
    JsonObject output = GSON.fromJson(PARSER.encode(results), JsonObject.class);

    Assertions.assertEquals(CONFIG.getOutLabels().size(), output.size());
    for(Map.Entry<String, JsonElement> entry : output.entrySet())
    {
      Assertions.assertEquals(results.get(entry.getKey()).data().toString(), entry.getValue().getAsJsonPrimitive().getAsString());
    }
  }
}