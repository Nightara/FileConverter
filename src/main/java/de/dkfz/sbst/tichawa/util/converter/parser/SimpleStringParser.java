package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import reactor.core.publisher.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

@Getter
@EqualsAndHashCode(callSuper=true)
public class SimpleStringParser extends ReactiveParser<String, String>
{
  private final String inSeparator;
  private final String outSeparator;

  public SimpleStringParser(String name, Path outputPath, String inSeparator, String outSeparator)
  {
    super(name, outputPath);
    this.inSeparator = inSeparator;
    this.outSeparator = outSeparator;
  }

  public SimpleStringParser(String name, Path outputPath, Configuration config, String inSeparator, String outSeparator, String... inHeaders)
  {
    super(name, outputPath, config, List.of(inHeaders));
    this.inSeparator = inSeparator;
    this.outSeparator = outSeparator;
  }

  @Override
  public Optional<String[]> parseHeaderLine(String input)
  {
    try
    {
      return Optional.of(input.split(getInSeparator()));
    }
    catch(NullPointerException ex)
    {
      return Optional.empty();
    }
  }

  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, String input)
  {
    if(isReady())
    {
      List<String> strippedData = Arrays.stream(input.split(getInSeparator(), -1))
          .map(line -> line.replace("\"", ""))
          .toList();
      if(strippedData.size() >= getInHeaders().size())
      {
        Map<String, Rule.Result<Object>> output = new HashMap<>();
        for(int x = 0; x < getInHeaders().size(); x++)
        {
          Optional<Rule<Object, Object>> filterStatus = getFilterStatus(getInHeaders().get(x), strippedData.get(x),false);
          if(filterStatus.isPresent())
          {
            return Mono.error(new FilterRule.FilterException(filterStatus.get(), lineNumber, input));
          }

          mapInto(getInHeaders().get(x), strippedData.get(x), output,false);
        }

        return output.isEmpty() ? Mono.error(new ParseException("Empty output data", lineNumber, input)) : Mono.just(new ParsedLine(lineNumber, output));
      }
      else
      {
        return Mono.error(new ParseException("Input data shorter than expected. Expected "
            + getInHeaders().size() + " elements, but found " + strippedData.size(), lineNumber, input));
      }
    }
    else
    {
      return Mono.error(new ParseException("Parser is not ready.", lineNumber, input));
    }
  }

  @Override
  public String encode(Map<String, Rule.Result<Object>> data)
  {
    return getConfig().getOutLabels().stream()
        .map(data::get)
        .filter(Objects::nonNull)
        .map(Rule.Result::data)
        .map(Object::toString)
        .map(this::format)
        .collect(Collectors.joining(getOutSeparator()));
  }

  @Override
  public List<String> encodeHeader(Collection<String> header)
  {
    return List.of(header.stream().collect(Collectors.joining(getOutSeparator())));
  }
}
