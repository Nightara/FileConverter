package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;

import java.nio.file.*;
import java.util.*;

public interface Parser<I, O>
{
  boolean configure(Configuration configuration, String... inHeaders);
  boolean isReady();

  String getName();
  Path getOutputPath();

  Optional<String[]> parseHeaderLine(I input);

  Map<String, Rule.Result<Object>> parse(I input);
  O encode(Map<String, Rule.Result<Object>> data);
  O encodeHeader();

  default O translate(I input)
  {
    return encode(parse(input));
  }

  default boolean hasOutputPath()
  {
    return getOutputPath() != null;
  }
}
