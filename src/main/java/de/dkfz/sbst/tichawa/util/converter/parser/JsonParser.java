package de.dkfz.sbst.tichawa.util.converter.parser;

import com.google.gson.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import reactor.core.publisher.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class JsonParser extends ReactiveParser<String, String>
{
  private final Gson gson;

  public JsonParser(String name, Path outputPath)
  {
    this(name, outputPath,null);
  }

  public JsonParser(String name, Path outputPath, Configuration config, String... inHeaders)
  {
    super(name, outputPath, config, List.of(inHeaders));
    this.gson = new GsonBuilder().create();
  }

  //TODO: Test
  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, String input)
  {
    if(isReady())
    {
      try
      {
        Map<String, Rule.Result<Object>> output = new HashMap<>();
        JsonObject json = gson.fromJson(input, JsonObject.class);
        for(Map.Entry<String, JsonElement> entry : json.entrySet())
        {
          Optional<Rule<Object, Object>> filterStatus = getFilterStatus(entry.getKey(), getFieldValue(entry.getValue()),true);
          if(filterStatus.isPresent())
          {
            return Mono.error(new FilterRule.FilterException(filterStatus.get(), lineNumber, input));
          }

          mapInto(entry.getKey(), getFieldValue(entry.getValue()), output,true);
        }

        return output.isEmpty() ? Mono.error(new ParseException("Empty output data", lineNumber, input)) : Mono.just(new ParsedLine(lineNumber, output));
      }
      catch(JsonSyntaxException ex)
      {
        return Mono.error(ex);
      }
    }
    else
    {
      return Mono.error(new ParseException("Parser is not ready.", lineNumber, input));
    }
  }

  @Override
  public Optional<String[]> parseHeaderLine(String input)
  {
    try
    {
      JsonObject parsed = gson.fromJson(input, JsonObject.class);
      return Optional.of(parsed.keySet().toArray(String[]::new));
    }
    catch(JsonSyntaxException ex)
    {
      return Optional.empty();
    }
  }

  @Override
  public String encode(ParsedLine data)
  {
    return gson.toJson(data.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey,entry -> entry.getValue().data())));
  }

  @Override
  public List<String> encodeHeader(Collection<String> header)
  {
    return List.of();
  }

  @Override
  public boolean isOutputEmpty(String output)
  {
    try
    {
      JsonObject parsed = gson.fromJson(output, JsonObject.class);
      return parsed.isEmpty();
    }
    catch(JsonSyntaxException ex)
    {
      return true;
    }
  }

  @Override
  public boolean isInputEmpty(String input)
  {
    try
    {
      JsonObject parsed = gson.fromJson(input, JsonObject.class);
      return parsed.isEmpty();
    }
    catch(JsonSyntaxException ex)
    {
      return true;
    }
  }

  private static Object getFieldValue(JsonElement jsonElement)
  {
    if(jsonElement.isJsonPrimitive())
    {
      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      if(jsonPrimitive.isBoolean())
      {
        return jsonPrimitive.getAsBoolean();
      }
      else if(jsonPrimitive.isNumber())
      {
        return jsonPrimitive.getAsNumber();
      }
      else if(jsonPrimitive.isString())
      {
        String value = jsonPrimitive.getAsString();
        try
        {
          return Instant.parse(value);
        }
        catch(DateTimeParseException ex)
        {
          return value;
        }
      }
    }

    return null;
  }
}
