package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import lombok.experimental.Delegate;

import java.nio.file.*;
import java.util.*;

/**
 * The base interface of the converter library.
 *
 * @param <I> The initial input type for the parser
 * @param <O> The final output type for the parser
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

  default O encodeEmptyLine()
  {
    return encode(ParsedLine.EMPTY);
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

  /**
   * The intermediate data format used by all parsers.
   */
  @Value
  class ParsedLine implements Map<String, Rule.Result<Object>>
  {
    public static final ParsedLine EMPTY = new ParsedLine(-1, Collections.emptyMap());

    int lineNumber;
    @Delegate
    @Getter(AccessLevel.NONE)
    Map<String, Rule.Result<Object>> innerMap;

    /**
     * Create a copy of this ParsedLine, prefixing all attributes with the given string prefix.
     *
     * @param prefix The prefix to prepend
     * @return The prefixed copy
     */
    public ParsedLine withPrefix(String prefix)
    {
      ParsedLine newParsedLine = new ParsedLine(lineNumber, new HashMap<>());
      this.forEach((key, entry) -> newParsedLine.put(prefix + key, entry));

      return newParsedLine;
    }

    /**
     * Create a copy of this ParsedLine, removing the given string prefix from all attributes.
     * Attributes without the prefix remain unchanged.
     *
     * @param prefix The prefix to remove
     * @return The copy with prefixes
     */
    public ParsedLine withoutPrefix(String prefix)
    {
      ParsedLine newParsedLine = new ParsedLine(lineNumber, new HashMap<>());
      this.forEach((key, entry) -> newParsedLine.put(key.startsWith(prefix) ? key.substring(prefix.length()) : key,
          entry));

      return newParsedLine;
    }

    /**
     * Create a merged copy of two ParsedLines, adding all attributes from both provided ParsedLines.
     * This method only works for ParsedLines sharing the same line number to avoid unintended merges, and only allows
     * merging of fully ParsedLines with fully disjointed attributes.
     *
     * @param a The first ParsedLine
     * @param b The second ParsedLine
     * @return The merged copy
     * @throws IllegalArgumentException If the two lines do not share the same line number or contain an overlap in
     * attributes
     */
    public static ParsedLine merge(ParsedLine a, ParsedLine b)
    {
      if(a.lineNumber != b.lineNumber || !Collections.disjoint(a.keySet(), b.keySet()))
      {
        throw new IllegalArgumentException("The ParsedLines must have the same line number and must be disjoint");
      }
      HashMap<String, Rule.Result<Object>> newMap = new HashMap<>(a);
      newMap.putAll(b);

      return new ParsedLine(a.lineNumber, newMap);
    }
  }
}
