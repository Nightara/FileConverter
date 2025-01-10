package de.dkfz.sbst.tichawa.util.converter.parser;

import com.google.gson.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import reactor.core.publisher.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class JsonParser extends ReactiveParser<JsonObject, JsonObject>
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

  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, JsonObject input)
  {
    if(isReady())
    {
      try
      {
        Map<String, Rule.Result<Object>> output = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : input.entrySet())
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
  public Optional<String[]> parseHeaderLine(JsonObject input)
  {
    try
    {
      return Optional.of(input.keySet().toArray(String[]::new));
    }
    catch(JsonSyntaxException ex)
    {
      return Optional.empty();
    }
  }

  @Override
  public JsonObject encode(ParsedLine data)
  {
    JsonObject output = new JsonObject();
    data.forEach((key,value) -> output.add(key, gson.toJsonTree(value.data())));

    return output;
  }

  @Override
  public List<JsonObject> encodeHeader(Collection<String> header)
  {
    return List.of();
  }

  @Override
  public boolean isOutputEmpty(JsonObject output)
  {
    return output.isEmpty();
  }

  @Override
  public boolean isInputEmpty(JsonObject input)
  {
    return input.isEmpty();
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
