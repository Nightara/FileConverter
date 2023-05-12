package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Rule.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

@SuppressWarnings("unchecked")
class ExcelParserTest
{
  private static final XSSFWorkbook CACHE = new XSSFWorkbook();
  private static final Sheet CACHE_SHEET = CACHE.createSheet();
  private static final Row ROW_ONE = CACHE_SHEET.createRow(CACHE_SHEET.getLastRowNum() + 1);
  private static final Row ROW_TWO = CACHE_SHEET.createRow(CACHE_SHEET.getLastRowNum() + 1);
  private static final Row ROW_ONE_CONV = CACHE_SHEET.createRow(CACHE_SHEET.getLastRowNum() + 1);
  private static final Row ROW_TWO_CONV = CACHE_SHEET.createRow(CACHE_SHEET.getLastRowNum() + 1);
  static
  {
    ROW_ONE.createCell(0, CellType.STRING).setCellValue("one");
    ROW_ONE.createCell(1, CellType.NUMERIC).setCellValue(2);

    ROW_TWO.createCell(0, CellType.STRING).setCellValue("two");
    ROW_TWO.createCell(1, CellType.NUMERIC).setCellValue(0);

    ROW_ONE_CONV.createCell(0, CellType.STRING).setCellValue("three");
    ROW_ONE_CONV.createCell(1, CellType.NUMERIC).setCellValue(2);

    ROW_TWO_CONV.createCell(0, CellType.STRING).setCellValue("one");
    ROW_TWO_CONV.createCell(1, CellType.NUMERIC).setCellValue(0);
  }

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
  private static final ExcelParser PARSER = new ExcelParser("Custom",null, CONFIG,
          "in_one", "in_two");

  @SuppressWarnings("unused")
  static List<Arguments> generateTestSets()
  {
    List<Arguments> arguments = new LinkedList<>();
    arguments.add(Arguments.of(ROW_ONE, ROW_ONE_CONV));
    arguments.add(Arguments.of(ROW_TWO, ROW_TWO_CONV));

    return arguments;
  }

  @ParameterizedTest
  @MethodSource("generateTestSets")
  void testTranslate(Row input, Row expected)
  {
    Row translated = PARSER.translate(input);
    for(int x = 0; x < ROW_ONE.getLastCellNum(); x++)
    {
      Cell expectedCell = expected.getCell(x);
      Cell actualCell = translated.getCell(x);
      if(expectedCell == null)
      {
        Assertions.assertNull(actualCell);
      }
      else
      {
        Assertions.assertNotNull(actualCell);
        Assertions.assertEquals(expectedCell.getCellType(), actualCell.getCellType());
        Assertions.assertEquals(getCellValue(expectedCell), getCellValue(actualCell));
      }
    }
  }

  @Test
  void testParse()
  {
    Map<String, Result<Object>> results = PARSER.parse(ROW_ONE);

    Assertions.assertEquals(2, results.size());
    Assertions.assertTrue(results.containsKey("out_one"));
    Assertions.assertTrue(results.containsKey("out_two"));
    Assertions.assertInstanceOf(Number.class, results.get("out_one").data());
    Assertions.assertInstanceOf(String.class, results.get("out_two").data());
  }

  @ParameterizedTest
  @MethodSource("generateTestSets")
  void testEncode(Row input)
  {
    Map<String, Result<Object>> results = PARSER.parse(input);
    Row output = PARSER.encode(results);

    Assertions.assertEquals(CONFIG.getOutLabels().size(), output.getLastCellNum());
    for(int x = 0; x < output.getLastCellNum(); x++)
    {
      Assertions.assertEquals(results.get(CONFIG.getOutLabels().get(x)).data(), getCellValue(output.getCell(x)));
    }
  }

  private static Object getCellValue(Cell cell)
  {
    return switch(cell.getCellType())
    {
      case _NONE, BLANK -> "";
      case NUMERIC -> cell.getNumericCellValue();
      case STRING -> cell.getStringCellValue();
      case BOOLEAN -> cell.getBooleanCellValue();
      case FORMULA -> cell.getCellFormula();
      case ERROR -> cell.getErrorCellValue();
    };
  }
}