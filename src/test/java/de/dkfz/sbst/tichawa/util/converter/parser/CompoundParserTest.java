package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.util.*;

class CompoundParserTest
{
  private static final XSSFWorkbook CACHE = new XSSFWorkbook();
  private static final Sheet INPUT_SHEET = CACHE.createSheet();
  private static final Sheet OUTPUT_SHEET = CACHE.createSheet();
  private static final Row ROW_ONE = INPUT_SHEET.createRow(INPUT_SHEET.getLastRowNum() + 1);
  private static final Row ROW_TWO = INPUT_SHEET.createRow(INPUT_SHEET.getLastRowNum() + 1);
  private static final Row ROW_ONE_CONV = OUTPUT_SHEET.createRow(OUTPUT_SHEET.getLastRowNum() + 1);
  private static final Row ROW_TWO_CONV = OUTPUT_SHEET.createRow(OUTPUT_SHEET.getLastRowNum() + 1);
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
    Rule<?, ?> firstRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"one","three");
    RULES.add((Rule<Object, Object>) firstRule);
    Rule<?, ?> secondRule = new SimpleRule<>("in_one","out_two", Configuration.DataType.STRING, Configuration.DataType.STRING, Rule.Mode.TRANSLATE,"two", "one");
    RULES.add((Rule<Object, Object>) secondRule);
    Rule<?, ?> thirdRule = new SimpleRule<>("in_two","out_one", Configuration.DataType.INTEGER, Configuration.DataType.INTEGER, Rule.Mode.KEEP,0,0);
    RULES.add((Rule<Object, Object>) thirdRule);
  }
  private static final Configuration CONFIG = new Configuration(RULES, new HashMap<>());
  private static final ExcelParser EXCEL_PARSER = new ExcelParser("Excel","CUSTOM_SHEET",Path.of("out.xlsx"),
      CONFIG,"in_one", "in_two");
  private static final SimpleStringParser STRING_PARSER = new SimpleStringParser("String", Path.of("out.tsv"),
      CONFIG,"\t","\t","in_one", "in_two");

  CompoundParser<String, Row> COMPOUND_PARSER_ONE = new CompoundParser<>("String to Excel", STRING_PARSER, EXCEL_PARSER);
  CompoundParser<Row, String> COMPOUND_PARSER_TWO = new CompoundParser<>("Excel to String", EXCEL_PARSER, STRING_PARSER);

  @Test
  void testSetups()
  {
    Assertions.assertIterableEquals(STRING_PARSER.getInHeaders(), COMPOUND_PARSER_ONE.getInHeaders());
    Assertions.assertIterableEquals(EXCEL_PARSER.getInHeaders(), COMPOUND_PARSER_TWO.getInHeaders());

    Assertions.assertEquals(EXCEL_PARSER.getOutputPath(), COMPOUND_PARSER_ONE.getOutputPath());
    Assertions.assertEquals(STRING_PARSER.getOutputPath(), COMPOUND_PARSER_TWO.getOutputPath());
  }

  @Test
  void testConversion()
  {
    Assertions.assertEquals(STRING_PARSER.parse("one\t2"), COMPOUND_PARSER_ONE.parse("one\t2"));
    Assertions.assertEquals(STRING_PARSER.parse("two\t0"), COMPOUND_PARSER_ONE.parse("two\t0"));

    Assertions.assertEquals(EXCEL_PARSER.parse(ROW_ONE), COMPOUND_PARSER_TWO.parse(ROW_ONE));
    Assertions.assertEquals(EXCEL_PARSER.parse(ROW_TWO), COMPOUND_PARSER_TWO.parse(ROW_TWO));

    compareRows(ROW_ONE_CONV, COMPOUND_PARSER_ONE.translate("one\t2"));
    compareRows(ROW_TWO_CONV, COMPOUND_PARSER_ONE.translate("two\t0"));

    Assertions.assertEquals("three\t2", COMPOUND_PARSER_TWO.translate(ROW_ONE));
    Assertions.assertEquals("one\t0", COMPOUND_PARSER_TWO.translate(ROW_TWO));
  }

  private static void compareRows(Row expectedRow, Row actualRow)
  {
    for(int x = 0; x < expectedRow.getLastCellNum(); x++)
    {
      Assertions.assertEquals(ExcelParserTest.getCellValue(expectedRow.getCell(x)),
          ExcelParserTest.getCellValue(actualRow.getCell(x + ExcelParser.DATA_OFFSET)));
    }
  }
}
