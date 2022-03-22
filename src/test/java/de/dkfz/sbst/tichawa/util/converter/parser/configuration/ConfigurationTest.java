package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.util.*;

class ConfigurationTest
{
  private static final String nullLine = null;
  private static final String blankLine = "";
  private static final String commentLine = "#";
  private static final String headerLine = "#IN_LABEL\tOUT_LABEL\tIN_TYPE\tOUT_TYPE\tMODE\tIN_VAL\tOUT_VAL";
  private static final String commentedValidLine = "#Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\t1\tone";
  private static final String tooShortLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\t1";
  private static final String tooLongLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\t1\tone\ttwo";
  private static final String wrongInTypeLine = "Test_IN\tTest_OUT\tint\tSTRING\tTRANSLATE\t1\tone";
  private static final String wrongOutTypeLine = "Test_IN\tTest_OUT\tINTEGER\ttext\tTRANSLATE\t1\tone";
  private static final String wrongModeLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tMAGIC\t1\tone";
  private static final String wrongInDataLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\tone\tone";
  private static final String wrongOutDataLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\tone\t1";
  private static final String validSyntaxLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tKEEP\t1\tone";
  private static final String validLine = "Test_IN\tTest_OUT\tINTEGER\tSTRING\tTRANSLATE\t1\tone";

  @SuppressWarnings("unused")
  static List<Arguments> generateTestSets()
  {
    List<Arguments> arguments = new LinkedList<>();
    arguments.add(Arguments.of(nullLine, false, false));
    arguments.add(Arguments.of(blankLine, false, false));
    arguments.add(Arguments.of(commentLine, false, false));
    arguments.add(Arguments.of(headerLine, false, false));
    arguments.add(Arguments.of(commentedValidLine, false, true));
    arguments.add(Arguments.of(tooShortLine, false, false));
    arguments.add(Arguments.of(tooLongLine, false, true));
    arguments.add(Arguments.of(wrongInTypeLine, false, false));
    arguments.add(Arguments.of(wrongOutTypeLine, false, false));
    arguments.add(Arguments.of(wrongModeLine, false, false));
    arguments.add(Arguments.of(wrongInDataLine, true, false));
    arguments.add(Arguments.of(wrongOutDataLine, true, false));
    arguments.add(Arguments.of(validSyntaxLine, true, true));
    arguments.add(Arguments.of(validLine, true, true));

    return arguments;
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings={"/test.cfg"})
  void testFromFile(String inputFile)
  {
    InputStream inputStream = Configuration.class.getResourceAsStream(inputFile);
    Assertions.assertNotNull(inputStream);
    Optional<Configuration> config = Configuration.fromFile(Configuration.class.getResourceAsStream(inputFile));
    long lineCount = new BufferedReader(new InputStreamReader(inputStream))
        .lines().count() - 1;
    Assertions.assertTrue(config.isPresent());
    Assertions.assertEquals(lineCount, config.get().getRules().size());
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  void testIsValidRule(String line, boolean isValid, boolean canParse)
  {
    Assertions.assertEquals(isValid, Configuration.isValidRule(line));
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  void testParseRule(String line, boolean isValid, boolean canParse)
  {
    Assertions.assertEquals(canParse, Configuration.parseRule(line).isPresent());
  }

  @ParameterizedTest
  @SuppressWarnings("unused")
  @MethodSource("generateTestSets")
  void testIsValidCatchesEdgeCases(String line, boolean isValid, boolean canParse)
  {
    boolean regularCase = !commentedValidLine.equals(line) && !tooLongLine.equals(line);

    // regularCase <=> canParse => isValid
    Assertions.assertEquals(regularCase,
        Configuration.parseRule(line).isEmpty() || Configuration.isValidRule(line));
  }
}