package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import lombok.*;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Value
@AllArgsConstructor(access=AccessLevel.PUBLIC)
public class Configuration
{
  private static final Map<DataType<?>, List<ConfigParser<?>>> PARSERS;
  private static final DateTimeFormatter EU_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  List<Rule<Object, Object>> rules;

  static
  {
    PARSERS = new HashMap<>();
    PARSERS.put(DataType.STRING, Collections.singletonList(new ConfigParser<>(DataType.STRING,
        input -> input.replaceAll("\"", ""))));
    PARSERS.put(DataType.INTEGER, Collections.singletonList(new ConfigParser<>(DataType.INTEGER,
        input ->  input.isBlank() ? null : Integer.parseInt(input))));
    PARSERS.put(DataType.DOUBLE, Collections.singletonList(new ConfigParser<>(DataType.DOUBLE,
        input -> input.isBlank() || input.equals("ND") ? null : Double.parseDouble(input))));
    PARSERS.put(DataType.BOOLEAN, Arrays.asList(new ConfigParser<>(DataType.BOOLEAN, Boolean::parseBoolean),
        new ConfigParser<>(DataType.BOOLEAN,
            input -> input.isBlank() ? null : Integer.parseInt(input) != 0)));
    PARSERS.put(DataType.INSTANT, Arrays.asList(new ConfigParser<>(DataType.INSTANT, Instant::parse),
        new ConfigParser<>(DataType.INSTANT,
            input ->
            {
              if(input.isBlank())
              {
                return null;
              }
              else if(input.equals("EPOCH"))
              {
                return Instant.EPOCH;
              }
              else
              {
                return EU_FORMATTER.parse(input, LocalDate::from)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
              }
            }),
        new ConfigParser<>(DataType.INSTANT,
            input ->
            {
              if(input.isBlank())
              {
                return null;
              }
              else if(input.equals("EPOCH"))
              {
                return Instant.EPOCH;
              }
              else
              {
                return TIME_FORMATTER.parse(input, LocalTime::from)
                    .atDate(LocalDate.EPOCH)
                    .toInstant(ZoneOffset.UTC);
              }
            })));
    PARSERS.put(DataType.LOCALDATE, Collections.singletonList(new ConfigParser<>(DataType.DURATION, input -> null)));
    PARSERS.put(DataType.DURATION, Collections.singletonList(new ConfigParser<>(DataType.DURATION,
            input -> input.isBlank() ? null : Duration.parse(input))));
  }

  public List<String> getOutLabels()
  {
    return getRules().stream()
        .map(Rule::getOutLabel)
        .distinct()
        .collect(Collectors.toList());
  }

  public static Optional<Configuration> fromFile(File f)
  {
    try
    {
      return fromFile(new FileInputStream(f));
    }
    catch(NullPointerException | IOException ex)
    {
      return Optional.empty();
    }
  }

  public static Optional<Configuration> fromFile(InputStream in)
  {
    if(in != null)
    {
      try(BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
      {
        List<Rule<Object, Object>> rules = reader.lines()
          .filter(Configuration::isValidRule)
          .map(Configuration::parseRule)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());

        Map<String, List<SumRule<Object>>> sumRules = rules.stream()
            .filter(SumRule.class::isInstance)
            .map(rule -> (SumRule<Object>) rule)
            .collect(Collectors.toMap(Rule::getOutLabel, List::of, (a, b) ->
            {
              List<SumRule<Object>> list = new LinkedList<>(a);
              list.addAll(b);
              return list;
            }));
        sumRules.values().forEach(ruleGroup -> ruleGroup.forEach(rule -> rule.setRuleGroup(ruleGroup)));

        return Optional.of(new Configuration(rules));
      }
      catch(IOException ex)
      {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  protected static boolean isValidRule(String line)
  {
    if(line == null)
    {
      return false;
    }
    String[] data = line.split("\t");

    return !data[0].startsWith("#")
        && data.length == 7
        && DataType.getDataType(data[2]).isPresent()
        && DataType.getDataType(data[3]).isPresent()
        && Rule.Mode.getMode(data[4]).isPresent();
  }

  @SuppressWarnings("unchecked")
  protected static <K, G> Optional<Rule<K, G>> parseRule(String line)
  {
    try
    {
      String[] data = line.split("\t");
      DataType<K> inType = (DataType<K>) DataType.getDataType(data[2]).orElse(null);
      DataType<G> outType = (DataType<G>) DataType.getDataType(data[3]).orElse(null);
      return Rule.Mode.getMode(data[4]).map(mode ->
      {
        K inData = getParsers(inType).stream()
            .map(parser -> parser.tryApply(data[5]))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
        G outData = getParsers(outType).stream()
            .map(parser ->
            {
              try
              {
                return parser.apply(data[6]);
              }
              catch(Exception ex)
              {
                return null;
              }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        if((inData == null && mode == Rule.Mode.TRANSLATE)
            || (outData == null && mode != Rule.Mode.KEEP && mode != Rule.Mode.SPECIAL && mode != Rule.Mode.SUM))
        {
          return null;
        }

        if(mode == Rule.Mode.SUM)
        {
          return (Rule<K, G>) new SumRule<>(data[0], data[1], inType, inData);
        }
        else
        {
          return new SimpleRule<>(data[0], data[1], inType, outType, mode, inData, outData);
        }
      });
    }
    catch(Exception ex)
    {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public static <K> List<ConfigParser<K>> getParsers(DataType<K> dataType)
  {
    List<ConfigParser<K>> parsers = new LinkedList<>();
    if(PARSERS.containsKey(dataType))
    {
      PARSERS.get(dataType).stream()
          .map(parser -> (ConfigParser<K>) parser)
          .forEach(parsers::add);
    }

    return parsers;
  }

  @Value
  public static class DataType<T>
  {
    private static final List<DataType<?>> DATA_TYPES = new LinkedList<>();

    public static final DataType<String> STRING = new DataType<>(String.class);
    public static final DataType<Integer> INTEGER = new DataType<>(Integer.class);
    public static final DataType<Double> DOUBLE = new DataType<>(Double.class);
    public static final DataType<Boolean> BOOLEAN = new DataType<>(Boolean.class);
    public static final DataType<Instant> INSTANT = new DataType<>(Instant.class);
    public static final DataType<LocalDate> LOCALDATE = new DataType<>(LocalDate.class);
    public static final DataType<Duration> DURATION = new DataType<>(Duration.class);

    Class<T> clazz;

    private DataType(Class<T> clazz)
    {
      this.clazz = clazz;
      DATA_TYPES.add(this);
    }

    public String getLabel()
    {
      return getClazz().getSimpleName().toUpperCase();
    }

    @SuppressWarnings("unchecked")
    public static <K> Optional<DataType<K>> getDataType(String label)
    {
      return DATA_TYPES.stream()
          .filter(type -> type.getLabel().equals(label))
          .map(type -> (DataType<K>) type)
          .findAny();
    }
  }

  @Value
  public static class ConfigParser<T> implements Function<String, T>
  {

    Configuration.DataType<T> dataType;
    Function<String, T> parser;

    @Override
    public T apply(String s)
    {
      return getParser().apply(s);
    }

    public Optional<T> tryApply(String s)
    {
      try
      {
        return Optional.ofNullable(getParser().apply(s));
      }
      catch(Exception ex)
      {
        return Optional.empty();
      }
    }
  }
}
