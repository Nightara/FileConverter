package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import reactor.core.publisher.*;

import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

@Value
@EqualsAndHashCode(callSuper=true)
public class ExcelParser extends ReactiveParser<Row, Row>
{
  public static final Integer DATA_OFFSET = 3;

  String sheetName;

  @Getter(AccessLevel.PRIVATE)
  Sheet cacheSheet;
  @Getter(AccessLevel.PRIVATE)
  XSSFWorkbook cache;

  public ExcelParser(String name, String sheetName, Path outputPath)
  {
    this(name, sheetName, outputPath,null);
  }

  public ExcelParser(String name, String sheetName, Path outputPath, Configuration config, String... inHeaders)
  {
    super(name, outputPath, config, List.of(inHeaders));
    this.sheetName = sheetName;
    this.cache = new XSSFWorkbook();
    this.cacheSheet = getCache().createSheet("Test");
  }

  @Override
  public Optional<String[]> parseHeaderLine(Row input)
  {
    return Optional.of(StreamSupport.stream(input.spliterator(),false)
        .map(ExcelParser::getCellContent)
        .map(Object::toString)
        .toArray(String[]::new));
  }

  @Override
  public Row encode(ParsedLine data)
  {
    Row output = cacheSheet.createRow(cacheSheet.getLastRowNum() + 1);
    List<String> labels = getConfig().getOutLabels();
    for(int x = 0; x < labels.size(); x++)
    {
      String label = labels.get(x);
      Cell cell = output.createCell(x + DATA_OFFSET);
      Rule.Result<Object> value = data.get(label);
      if(value == null)
      {
        cell.setBlank();
      }
      else if(value.rule().getOutType().equals(Configuration.DataType.STRING))
      {
        cell.setCellValue(format(value.data().toString()));
      }
      else if(value.rule().getOutType().equals(Configuration.DataType.BOOLEAN))
      {
        cell.setCellValue(value.data().toString());
      }
      else if(value.rule().getOutType().equals(Configuration.DataType.INTEGER)
          || value.rule().getOutType().equals(Configuration.DataType.DOUBLE))
      {
        if(value.data() instanceof Double doubleValue)
        {
          cell.setCellValue(doubleValue);
        }
        else
        {
          cell.setCellValue(0.0 + (Integer) value.data());
        }
      }
      else if(value.rule().getOutType().equals(Configuration.DataType.INSTANT)
          || value.rule().getOutType().equals(Configuration.DataType.LOCAL_DATE))
      {
        cell.setCellValue(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.ENGLISH).withZone(ZoneOffset.UTC)
            .format((TemporalAccessor) value.data()));
      }
      else if(value.rule().getOutType().equals(Configuration.DataType.DURATION))
      {
        cell.setCellValue(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.ENGLISH).withZone(ZoneOffset.UTC)
            .format(LocalDate.EPOCH.plus((Duration) value.data())));
      }
    }

    return output;
  }

  @Override
  public List<Row> encodeHeader(Collection<String> header)
  {
    Row lineOne = cacheSheet.createRow(0);
    lineOne.createCell(0).setCellValue("SAMPLE");
    Row lineTwo = cacheSheet.createRow(1);
    lineTwo.createCell(0).setCellValue("Sample type");
    Row lineThree = cacheSheet.createRow(2);
    lineThree.createCell(0).setCellValue(getSheetName());
    Row lineFour = cacheSheet.createRow(3);
    lineFour.createCell(0).setCellValue("$");
    lineFour.createCell(1).setCellValue("Parents");
    lineFour.createCell(2).setCellValue("Children");
    List<String> labels = getConfig().getOutLabels();
    for(int x = 0; x < labels.size(); x++)
    {
      lineFour.createCell(x + 3).setCellValue(labels.get(x));
    }

    return List.of(lineOne, lineTwo, lineThree, lineFour);
  }

  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, Row input)
  {
    if(isReady())
    {
      if(input.getLastCellNum() + 1 >= getInHeaders().size())
      {
        Map<String, Rule.Result<Object>> output = new HashMap<>();
        for(int x = 0; x < getInHeaders().size(); x++)
        {
          Optional<Rule<Object, Object>> filterStatus = getFilterStatus(getInHeaders().get(x), getCellContent(input.getCell(x)),true);
          if(filterStatus.isPresent())
          {
            return Mono.error(new FilterRule.FilterException(filterStatus.get(), lineNumber, input));
          }

          mapInto(getInHeaders().get(x), getCellContent(input.getCell(x)), output,true);
        }

        return output.isEmpty() ? Mono.error(new ParseException("Empty output data", lineNumber, input)) : Mono.just(new ParsedLine(lineNumber, output));
      }
      else
      {
        return Mono.error(new ParseException("Input data shorter than expected. Expected "
            + getInHeaders().size() + " elements, but found " + (input.getLastCellNum() + 1), lineNumber, input));
      }
    }
    else
    {
      return Mono.error(new ParseException("Parser is not ready.", lineNumber, input));
    }
  }

  private static Object getCellContent(Cell cell)
  {
    return switch(cell.getCellType())
    {
      case _NONE, BLANK, ERROR -> "";
      case BOOLEAN -> cell.getBooleanCellValue();
      case NUMERIC -> cell.getNumericCellValue();
      case STRING -> cell.getStringCellValue();
      case FORMULA -> cell.getCellFormula();
    };
  }

  // https://stackoverflow.com/questions/5785724/how-to-insert-a-row-between-two-rows-in-an-existing-excel-with-hssf-apache-poi
  public static void copyRow(Row source, Sheet target, int targetRow)
  {
    // Get the source / new row
    Row newRow = target.getRow(targetRow);

    // If the row exist in destination, push down all rows by 1 else create a new row
    if(newRow != null)
    {
      target.shiftRows(targetRow, target.getLastRowNum(),1);
    }
    else
    {
      newRow = target.createRow(targetRow);
    }

    // Loop through source columns to add to new row
    for (int i = 0; i < source.getLastCellNum(); i++)
    {
      // Grab a copy of the old/new cell
      Cell oldCell = source.getCell(i);
      Cell newCell = newRow.createCell(i);

      // If the old cell is null jump to next cell
      if (oldCell == null)
      {
        continue;
      }

      // If there is a cell comment, copy
      if(oldCell.getCellComment() != null)
      {
        newCell.setCellComment(oldCell.getCellComment());
      }

      // If there is a cell hyperlink, copy
      if(oldCell.getHyperlink() != null)
      {
        newCell.setHyperlink(oldCell.getHyperlink());
      }

      // Set the cell data value
      switch(oldCell.getCellType())
      {
        case STRING, BLANK, _NONE -> newCell.setCellValue(oldCell.getStringCellValue());
        case BOOLEAN -> newCell.setCellValue(oldCell.getBooleanCellValue());
        case ERROR -> newCell.setCellErrorValue(oldCell.getErrorCellValue());
        case FORMULA -> newCell.setCellFormula(oldCell.getCellFormula());
        case NUMERIC -> newCell.setCellValue(oldCell.getNumericCellValue());
      }
    }
  }

  @Override
  public boolean isOutputEmpty(Row output)
  {
    return output == null || output.getPhysicalNumberOfCells() == 0
        || StreamSupport.stream(output.spliterator(),false)
        .map(Cell::getCellType)
        .allMatch(CellType.BLANK::equals);
  }

  @Override
  public boolean isInputEmpty(Row input)
  {
    return isOutputEmpty(input);
  }
}
