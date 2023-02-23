package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import reactor.core.publisher.*;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Data
@With
@AllArgsConstructor
@RequiredArgsConstructor
public class SimpleStringParser implements ReactiveParser<String, String>
{
  private static final Pattern INSTANT_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})" +
      "T(\\d{2}):(\\d{2}):(\\d{2})Z$");
  private final String name;
  private final Path outputPath;
  private final String inSeparator;
  private final String outSeparator;

  private Configuration config;
  private List<String> inHeaders = Collections.emptyList();

  @Override
  public boolean configure(Configuration config, String... inHeaders)
  {
    this.config = config;
    this.inHeaders = Arrays.asList(inHeaders);
    return isReady();
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

  public boolean isReady()
  {
    return getConfig() != null && !getInHeaders().isEmpty();
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
          Optional<Rule<Object, Object>> filterStatus = getFilterStatus(getInHeaders().get(x), strippedData.get(x));
          if(filterStatus.isPresent())
          {
            return Mono.error(new FilterRule.FilterException(filterStatus.get(), lineNumber, input));
          }

          parseInto(getInHeaders().get(x), strippedData.get(x), output);
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

  private void parseInto(String label, String data, Map<String, Rule.Result<Object>> output)
  {
    getConfig().rules().stream()
        .filter(rule -> rule.getInLabel().equals(label))
        .map(rule -> Configuration.getParsers(rule.getInType()).stream()
            .map(parser ->
            {
              try
              {
                return parser.apply(data);
              }
              catch(Exception ex)
              {
                return ex;
              }
            }).filter(obj -> !(obj instanceof Exception))
            .filter(rule::canApply)
            .map(rule::tryApply)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(result -> output.putIfAbsent(result.label(), result));
  }

  private Optional<Rule<Object, Object>> getFilterStatus(String label, String data)
  {
    return getConfig().rules().stream()
        .filter(rule -> rule.getMode() == Rule.Mode.FILTER)
        .filter(rule -> rule.getInLabel().equals(label))
        .filter(rule -> Configuration.getParsers(rule.getInType()).stream()
            .map(parser ->
            {
              try
              {
                return parser.apply(data);
              }
              catch(Exception ex)
              {
                return ex;
              }
            }).filter(obj -> !(obj instanceof Exception))
            .anyMatch(rule::canApply))
        .findAny();
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

  protected String format(String input)
  {
    Matcher instant = INSTANT_PATTERN.matcher(input);
    if(instant.matches())
    {
      return String.format("%s-%s-%s %s:%s:%s", instant.group(1), instant.group(2), instant.group(3),
          instant.group(4), instant.group(5), instant.group(6));
    }
    else
    {
      return input;
    }
  }

  @Override
  public List<String> getHeaders()
  {
    return getConfig().getOutLabels();
  }

  @Override
  public String encodeHeader(Collection<String> header)
  {
    return header.stream()
        .collect(Collectors.joining(getOutSeparator()));
  }
}
