package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import reactor.core.publisher.*;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.*;
import java.util.stream.*;

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

  private record Tuple<T1, T2>(T1 t1, T2 t2)
  {
    public static <T1, T2> Tuple<T1, T2> of(T1 t1, T2 t2)
    {
      return new Tuple<>(t1, t2);
    }
  }

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

  protected Optional<Rule<Object, Object>> getFilterStatus(String label, Object data, boolean parsed)
  {
    return getConfig().rules().stream()
        .filter(rule -> rule.getMode() == Rule.Mode.FILTER)
        .filter(rule -> rule.getInLabel().equals(label))
        .filter(rule -> parsed ? rule.canApply(data) : tryParsers(data, rule).anyMatch(rule::canApply))
        .findAny();
  }

  protected void mapInto(String label, Object data, Map<String, Rule.Result<Object>> output, boolean parsed)
  {
    getConfig().rules().stream()
        .filter(rule -> rule.getInLabel().equals(label))
        .map(rule -> Tuple.of(rule, parsed ? data : getFirst(tryParsers(data, rule))))
        .filter(tuple -> tuple.t1().canApply(tuple.t2()))
        .map(tuple -> tuple.t1().tryApply(tuple.t2()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(result -> output.putIfAbsent(result.label(), result));
  }

  private static <T> T getFirst(Stream<T> stream)
  {
    AtomicReference<T> object = new AtomicReference<>();
    stream.limit(1).forEach(object::set);
    return object.get();
  }

  private static Stream<Object> tryParsers(Object data, Rule<Object, Object> rule)
  {
    return Configuration.getParsers(rule.getInType()).stream()
        .map(parser ->
        {
          try
          {
            return parser.apply(data.toString());
          }
          catch(Exception ex)
          {
            return ex;
          }
        }).filter(obj -> !(obj instanceof Exception));
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
