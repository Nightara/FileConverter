package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import reactor.core.publisher.*;

import java.util.*;

public interface ReactiveParser<I, O> extends Parser<I, O>
{
  Mono<ParsedLine> parseReactive(int lineNumber, I input);

  default ParsedLine parse(int lineNumber, I input)
  {
    return parseReactive(lineNumber, input)
        .onErrorReturn(new ParsedLine(0, new HashMap<>()))
        .block();
  }
}
