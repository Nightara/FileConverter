package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Data
@With
@AllArgsConstructor
@RequiredArgsConstructor
public class SimpleStringParser implements Parser<String, String>
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
  public Map<String, Rule.Result<Object>> parse(String input)
  {
    Map<String, Rule.Result<Object>> output = new HashMap<>();
    if(isReady())
    {
      List<String> strippedData = Arrays.stream(input.split(getInSeparator()))
          .map(line -> line.replace("\"", ""))
          .collect(Collectors.toList());
      if(strippedData.size() >= getInHeaders().size())
      {
        for(int x = 0; x < getInHeaders().size(); x++)
        {
          int finalX = x;
          getConfig().getRules().stream()
              .filter(rule -> rule.getInLabel().equals(getInHeaders().get(finalX)))
              .map(rule -> Configuration.getParsers(rule.getInType()).stream()
                  .map(parser ->
                  {
                    try
                    {
                      return parser.apply(strippedData.get(finalX));
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
              .forEach(result -> output.putIfAbsent(result.getLabel(), result));
        }
      }
    }

    return output;
  }

  @Override
  public String encode(Map<String, Rule.Result<Object>> data)
  {
    return getConfig().getOutLabels().stream()
        .map(data::get)
        .filter(Objects::nonNull)
        .map(Rule.Result::getData)
        .map(Object::toString)
        .map(SimpleStringParser::fixEncoding)
        .collect(Collectors.joining(getOutSeparator()));
  }

  protected static String fixEncoding(String input)
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
  public String encodeHeader()
  {
    return getConfig().getOutLabels().stream()
        .collect(Collectors.joining(getOutSeparator()));
  }
}
