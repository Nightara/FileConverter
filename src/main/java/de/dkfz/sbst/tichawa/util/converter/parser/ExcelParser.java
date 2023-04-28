package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

// TODO: Write Unit Tests
@Value
@EqualsAndHashCode(callSuper=true)
public class ExcelParser extends ReactiveParser<Row, Row>
{
  @Getter(AccessLevel.PRIVATE)
  XSSFWorkbook cache = new XSSFWorkbook();
  @Getter(AccessLevel.PRIVATE)
  Sheet cacheSheet = getCache().createSheet();

  public ExcelParser(String name, Path outputPath)
  {
    super(name, outputPath);
  }

  public ExcelParser(String name, Path outputPath, Configuration config, String... inHeaders)
  {
    super(name, outputPath, config, List.of(inHeaders));
  }

  @Override
  public Optional<String[]> parseHeaderLine(Row input)
  {
    return Optional.of(StreamSupport.stream(input.spliterator(),false)
        .map(ExcelParser::getCellContent)
        .toArray(String[]::new));
  }

  @Override
  public Row encode(Map<String, Rule.Result<Object>> data)
  {
    Row output = cacheSheet.createRow(cacheSheet.getLastRowNum() + 1);
    List<String> labels = getConfig().getOutLabels();
    for(int x = 0; x < labels.size(); x++)
    {
      String label = labels.get(x);
      if(data.containsKey(label))
      {
        output.createCell(x).setCellValue(format(data.get(label).data().toString()));
      }
    }

    return output;
  }

  @Override
  public Row encodeHeader(Collection<String> header)
  {
    Row outHeader = cacheSheet.createRow(0);
    List<String> labels = getConfig().getOutLabels();
    for(int x = 0; x < labels.size(); x++)
    {
      outHeader.createCell(x).setCellValue(labels.get(x));
    }

    return outHeader;
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
          // TODO: Rewrite using cell types.
          Optional<Rule<Object, Object>> filterStatus = getFilterStatus(getInHeaders().get(x), getCellContent(input.getCell(x)));
          if(filterStatus.isPresent())
          {
            return Mono.error(new FilterRule.FilterException(filterStatus.get(), lineNumber, serialize(input)));
          }

          // TODO: Rewrite using cell types.
          parseInto(getInHeaders().get(x), getCellContent(input.getCell(x)), output);
        }

        return output.isEmpty() ? Mono.error(new ParseException("Empty output data", lineNumber, serialize(input))) : Mono.just(new ParsedLine(lineNumber, output));
      }
      else
      {
        return Mono.error(new ParseException("Input data shorter than expected. Expected "
            + getInHeaders().size() + " elements, but found " + (input.getLastCellNum() + 1), lineNumber, serialize(input)));
      }
    }
    else
    {
      return Mono.error(new ParseException("Parser is not ready.", lineNumber, serialize(input)));
    }
  }

  private static String getCellContent(Cell cell)
  {
    return switch(cell.getCellType())
    {
      case _NONE, BLANK, ERROR -> "";
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case NUMERIC -> String.valueOf(cell.getNumericCellValue());
      case STRING -> cell.getStringCellValue();
      case FORMULA -> cell.getCellFormula();
    };
  }

  private static String[] serialize(Row input)
  {
    return StreamSupport.stream(input.spliterator(),false)
        .map(ExcelParser::getCellContent)
        .toArray(String[]::new);
  }
}
