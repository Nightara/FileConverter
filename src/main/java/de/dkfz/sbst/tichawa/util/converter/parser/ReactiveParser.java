package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import reactor.core.publisher.*;

import java.util.*;

public interface ReactiveParser<I, O> extends Parser<I, O>
{
  Mono<Map<String, Rule.Result<Object>>> parseReactive(I input);

  default Map<String, Rule.Result<Object>> parse(I input)
  {
    return parseReactive(input).block();
  }
}
