package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import org.junit.jupiter.api.*;

import java.util.*;

@SuppressWarnings("unchecked")
class MultiParserTest
{
  private static final List<Rule<Object, Object>> RULES_ONE = new LinkedList<>();
  static
  {
    Rule<?, ?> firstRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"one","three");
    RULES_ONE.add((Rule<Object, Object>) firstRule);
    Rule<?, ?> secondRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"two", "one");
    RULES_ONE.add((Rule<Object, Object>) secondRule);
    Rule<?, ?> thirdRule = new SimpleRule<>("in_two","out_one", Configuration.DataType.INTEGER, Configuration.DataType.INTEGER, Rule.Mode.KEEP,0,0);
    RULES_ONE.add((Rule<Object, Object>) thirdRule);
  }
  private static final List<Rule<Object, Object>> RULES_TWO = new LinkedList<>();
  static
  {
    Rule<?, ?> firstRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"one","four");
    RULES_TWO.add((Rule<Object, Object>) firstRule);
    Rule<?, ?> secondRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"two", "five");
    RULES_TWO.add((Rule<Object, Object>) secondRule);
    Rule<?, ?> thirdRule = new SimpleRule<>("in_three","out_three", Configuration.DataType.INTEGER, Configuration.DataType.INTEGER, Rule.Mode.KEEP,0,0);
    RULES_TWO.add((Rule<Object, Object>) thirdRule);
  }

  private static final String HEADER_LINE = "in_one\tin_two\tin_three";
  private static final String[] HEADER_LINES = new String[]{"in_one", "in_two", "in_three"};

  private static MultiParser<String, String> MULTI_PARSER;
  private static SimpleStringParser PARSER_ONE, PARSER_TWO;

  @BeforeEach
  void setup()
  {
    PARSER_ONE = new SimpleStringParser("Custom",null, new Configuration(RULES_ONE, new HashMap<>()),
        "\t","\n","in_one", "in_two");
    PARSER_TWO = new SimpleStringParser("Custom",null, new Configuration(RULES_TWO, new HashMap<>()),
        "\t","\n","in_one", "in_three");
    MULTI_PARSER = new MultiParser<>("MultiParser",null,List.of(PARSER_ONE, PARSER_TWO));
  }

  @Test
  void configure()
  {
    Assertions.assertLinesMatch(List.of("in_one", "in_two"), PARSER_ONE.getInHeaders());
    Assertions.assertLinesMatch(List.of("in_one", "in_three"), PARSER_TWO.getInHeaders());

    MULTI_PARSER.configure(HEADER_LINES);

    Assertions.assertLinesMatch(List.of(HEADER_LINES), MULTI_PARSER.getInHeaders());
    Assertions.assertLinesMatch(MULTI_PARSER.getInHeaders(), PARSER_ONE.getInHeaders());
    Assertions.assertLinesMatch(MULTI_PARSER.getInHeaders(), PARSER_TWO.getInHeaders());
  }

  @Test
  void parseHeaderLine()
  {
    MULTI_PARSER.configure(HEADER_LINES);

    Assertions.assertTrue(MULTI_PARSER.parseHeaderLine(HEADER_LINE).isPresent());
    Assertions.assertArrayEquals(HEADER_LINES, MULTI_PARSER.parseHeaderLine(HEADER_LINE).get());
  }

  @Test
  void parse()
  {
    MULTI_PARSER.configure(HEADER_LINES);
    Parser.ParsedLine resultsMulti = MULTI_PARSER.parse("one\t2\t3");
    Parser.ParsedLine resultsOne = PARSER_ONE.parse("one\t2\t3");
    Parser.ParsedLine resultsTwo = PARSER_TWO.parse("one\t2\t3");
    List<Parser.ParsedLine> resultsOneAndTwo = List.of(resultsOne, resultsTwo);

    Assertions.assertEquals(resultsOne.size() + resultsTwo.size(), resultsMulti.size());
    for(int p = 0; p < MULTI_PARSER.innerParsers.size(); p++)
    {
      for(String header : MULTI_PARSER.innerParsers.get(p).getHeaders())
      {
        Assertions.assertTrue(resultsMulti.containsKey(p + "-" + header));
        Assertions.assertEquals(resultsOneAndTwo.get(p).get(header), resultsMulti.get(p + "-" + header));
      }
    }
  }

  @Test
  void encode()
  {
    MULTI_PARSER.configure(HEADER_LINES);
    Parser.ParsedLine resultsMulti = MULTI_PARSER.parse("one\t2\t3");
    Parser.ParsedLine resultsOne = PARSER_ONE.parse("one\t2\t3");
    Parser.ParsedLine resultsTwo = PARSER_TWO.parse("one\t2\t3");

    List<String> encodedResults = MULTI_PARSER.encode(resultsMulti);

    Assertions.assertEquals(MULTI_PARSER.innerParsers.size(), encodedResults.size());
    Assertions.assertEquals(PARSER_ONE.encode(resultsOne), encodedResults.get(0));
    Assertions.assertEquals(PARSER_TWO.encode(resultsTwo), encodedResults.get(1));
  }

  @Test
  void encodeHeader()
  {
    MULTI_PARSER.configure(HEADER_LINES);

    Assertions.assertEquals(MULTI_PARSER.innerParsers.size(), MULTI_PARSER.encodeHeader().size());
    Assertions.assertEquals(PARSER_ONE.encodeHeader(), MULTI_PARSER.encodeHeader().get(0));
    Assertions.assertEquals(PARSER_TWO.encodeHeader(), MULTI_PARSER.encodeHeader().get(1));
  }

  @Test
  void isOutputEmpty()
  {
    MULTI_PARSER.configure(HEADER_LINES);

    Assertions.assertTrue(MULTI_PARSER.isOutputEmpty(List.of()));
    Assertions.assertTrue(MULTI_PARSER.isOutputEmpty(List.of("")));
    Assertions.assertTrue(MULTI_PARSER.isOutputEmpty(List.of("", "")));
    Assertions.assertTrue(MULTI_PARSER.isOutputEmpty(List.of(" ", " ")));

    Assertions.assertFalse(MULTI_PARSER.isOutputEmpty(List.of("test", "")));
    Assertions.assertFalse(MULTI_PARSER.isOutputEmpty(List.of("", "test")));
    Assertions.assertFalse(MULTI_PARSER.isOutputEmpty(List.of("test", "test")));
  }

  @Test
  void isInputEmpty()
  {
    MULTI_PARSER.configure(HEADER_LINES);

    Assertions.assertTrue(MULTI_PARSER.isInputEmpty(""));
    Assertions.assertTrue(MULTI_PARSER.isInputEmpty(" "));
    Assertions.assertTrue(MULTI_PARSER.isInputEmpty(null));

    Assertions.assertFalse(MULTI_PARSER.isInputEmpty("test"));
  }
}