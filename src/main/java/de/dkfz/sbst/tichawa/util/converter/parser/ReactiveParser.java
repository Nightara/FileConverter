package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Rule;
import lombok.*;
import reactor.core.publisher.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public abstract class ReactiveParser<I, O> implements Parser<I, O>
{
  private static final Pattern INSTANT_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})" +
      "T(\\d{2}):(\\d{2}):(\\d{2})Z$");

  private final String name;
  private final Path outputPath;

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
  public boolean isReady()
  {
    return getConfig() != null && !getInHeaders().isEmpty();
  }

  @Override
  public List<String> getHeaders()
  {
    return getConfig().getOutLabels();
  }

  public abstract Mono<ParsedLine> parseReactive(int lineNumber, I input);

  public ParsedLine parse(int lineNumber, I input)
  {
    return parseReactive(lineNumber, input)
        .onErrorReturn(new ParsedLine(0, new HashMap<>()))
        .block();
  }

  // TODO: Combine with getFilterStatus
  protected Optional<Rule<Object, Object>> parseAndGetFilterStatus(String label, String data)
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

  protected Optional<Rule<Object, Object>> getFilterStatus(String label, Object data)
  {
    return getConfig().rules().stream()
        .filter(rule -> rule.getMode() == Rule.Mode.FILTER)
        .filter(rule -> rule.getInLabel().equals(label))
        .filter(rule -> rule.canApply(data))
        .findAny();
  }

  // TODO: Combine with translateInto
  protected void parseInto(String label, String data, Map<String, Rule.Result<Object>> output)
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

  protected void translateInto(String label, Object data, Map<String, Rule.Result<Object>> output)
  {
    getConfig().rules().stream()
        .filter(rule -> rule.getInLabel().equals(label))
        .filter(rule -> rule.canApply(data))
        .map(rule -> rule.tryApply(data))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(result -> output.putIfAbsent(result.label(), result));
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
}
