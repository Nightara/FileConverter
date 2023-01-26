package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;

import java.nio.file.*;
import java.util.*;

/**
 * The base interface of the converter library.
 * TODO: Add explanation
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

  Map<String, Rule.Result<Object>> parse(I input);
  O encode(Map<String, Rule.Result<Object>> data);
  List<String> getHeader();
  O encodeHeader(Collection<String> header);

  default O encodeHeader()
  {
    return encodeHeader(getHeader());
  }

  default O translate(I input)
  {
    return encode(parse(input));
  }

  default boolean hasOutputPath()
  {
    return getOutputPath() != null;
  }
}
