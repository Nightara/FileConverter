package de.dkfz.sbst.tichawa.util.converter.parser;

import lombok.*;
import reactor.core.publisher.*;

import java.util.*;

/**
 * A Parser implementation that capable of conversion from any input type to any output type by delegating its methods
 * to an internal input and output sub parser.
 *
 * @param <I> The input type
 * @param <O> The output type
 */
@EqualsAndHashCode(callSuper=true)
public class CompoundParser<I, O> extends ReactiveParser<I, O>
{
  private final ReactiveParser<I, ?> inputParser;
  private final ReactiveParser<?, O> outputParser;

  public CompoundParser(String name, ReactiveParser<I, ?> inputParser, ReactiveParser<?, O> outputParser)
  {
    super(name, outputParser.getOutputPath(), inputParser.getConfig(), inputParser.getInHeaders());

    this.inputParser = inputParser;
    this.outputParser = outputParser;
  }

  @Override
  public Optional<String[]> parseHeaderLine(I input)
  {
    return inputParser.parseHeaderLine(input);
  }

  @Override
  public O encode(ParsedLine data)
  {
    return outputParser.encode(data);
  }

  @Override
  public List<O> encodeHeader(Collection<String> header)
  {
    return outputParser.encodeHeader(header);
  }

  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, I input)
  {
    return inputParser.parseReactive(lineNumber, input);
  }

  @Override
  public boolean isOutputEmpty(O output)
  {
    return outputParser.isOutputEmpty(output);
  }

  @Override
  public boolean isInputEmpty(I input)
  {
    return inputParser.isInputEmpty(input);
  }
}
