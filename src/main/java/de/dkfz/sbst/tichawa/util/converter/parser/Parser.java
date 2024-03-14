package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import lombok.experimental.Delegate;

import java.nio.file.*;
import java.util.*;

/**
 * The base interface of the converter library.
 *
 * @param <I> The initial input type for the parser.
 * @param <O> The final output type for the parser.
 */
@SuppressWarnings("unused")
public interface Parser<I, O>
{
  boolean configure(Configuration configuration, String... inHeaders);
  boolean isReady();

  String getName();
  Path getOutputPath();

  Optional<String[]> parseHeaderLine(I input);

  ParsedLine parse(int lineNumber, I input);
  O encode(ParsedLine data);
  List<String> getHeaders();
  List<O> encodeHeader(Collection<String> header);
  boolean isOutputEmpty(O output);
  boolean isInputEmpty(I input);

  default List<O> encodeHeader()
  {
    return encodeHeader(getHeaders());
  }

  default ParsedLine parse(I input)
  {
    return parse(0, input);
  }

  default O translate(I input)
  {
    return encode(parse(input));
  }

  default boolean hasOutputPath()
  {
    return getOutputPath() != null;
  }

  @Value
  class ParsedLine implements Map<String, Rule.Result<Object>>
  {
    int lineNumber;
    @Delegate
    @Getter(AccessLevel.NONE)
    Map<String, Rule.Result<Object>> innerMap;
  }
}
