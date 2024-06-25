package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import lombok.*;
import reactor.core.publisher.*;

import java.nio.file.*;
import java.util.*;

@EqualsAndHashCode(callSuper=true)
public class MultiParser<I, O> extends ReactiveParser<I, List<O>>
{
  protected final List<ReactiveParser<I, O>> innerParsers;

  public MultiParser(String name, Path outputPath, List<ReactiveParser<I, O>> innerParsers)
  {
    super(name, outputPath);
    this.innerParsers = innerParsers;
  }

  public boolean configure(String... inHeaders)
  {
    return configure(new Configuration(List.of(), Map.of()), inHeaders);
  }

  @Override
  public boolean configure(Configuration config, String... inHeaders)
  {
    super.configure(config, inHeaders);
    innerParsers.forEach(parser -> parser.setInHeaders(getInHeaders()));
    return innerParsers.stream()
        .map(Parser::isReady)
        .reduce(isReady(), Boolean::logicalAnd);
  }

  @Override
  public Optional<String[]> parseHeaderLine(I input)
  {
    return innerParsers.stream()
        .map(parser -> parser.parseHeaderLine(input))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  @Override
  public Mono<ParsedLine> parseReactive(int lineNumber, I input)
  {
    return Flux.fromIterable(innerParsers)
        .flatMap(parser -> parser.parseReactive(lineNumber, input)
            .map(parsedLine -> parsedLine.withPrefix(innerParsers.indexOf(parser) + "-")))
        .reduce(ParsedLine::merge);
  }

  @Override
  public List<O> encode(ParsedLine data)
  {
    return innerParsers.stream()
        .map(parser -> parser.encode(data.withoutPrefix(innerParsers.indexOf(parser) + "-")))
        .toList();
  }

  @Override
  public List<List<O>> encodeHeader()
  {
    return innerParsers.stream()
        .map(Parser::encodeHeader)
        .toList();
  }

  @Override
  public List<List<O>> encodeHeader(Collection<String> header)
  {
    return innerParsers.stream()
        .map(parser -> parser.encodeHeader(header))
        .toList();
  }

  @Override
  public boolean isOutputEmpty(List<O> output)
  {
    boolean empty = output.isEmpty() || output.size() < innerParsers.size();

    boolean allInnerEmpty = true;
    for(int p = 0; p < Math.min(output.size(), innerParsers.size()); p++)
    {
      allInnerEmpty &= innerParsers.get(p).isOutputEmpty(output.get(p));
    }

    return empty || allInnerEmpty;
  }

  @Override
  public boolean isInputEmpty(I input)
  {
    return innerParsers.stream().allMatch(parser -> parser.isInputEmpty(input));
  }
}
