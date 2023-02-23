package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import lombok.*;

import java.io.*;

@Value
@EqualsAndHashCode(callSuper=true)
public class FilterRule<I, O> extends Rule<I, O>
{
  Rule<I, O> innerRule;

  public FilterRule(Rule<I, O> innerRule)
  {
    super(innerRule.getInLabel(), innerRule.getOutLabel(), innerRule.getInType(), innerRule.getOutType(), Mode.FILTER,
        innerRule.getInVal(), innerRule.getOutVal());
    this.innerRule = innerRule;
  }

  @Override
  public boolean canApply(I value)
  {
    return getInnerRule().canApply(value);
  }

  @Override
  public Result<O> apply(I value)
  {
    return null;
  }

  @Override
  public boolean canReverse(O value)
  {
    return getInnerRule().canReverse(value);
  }

  @Override
  public Result<I> reverse(O value)
  {
    return null;
  }

  @Getter
  @EqualsAndHashCode(callSuper=true)
  @SuppressWarnings("java:S2166")
  public static class FilterException extends ParseException
  {
    private final Rule<?, ?> rule;

    public FilterException(Rule<?, ?> rule, int lineNumber, Serializable data)
    {
      super("FilterRule " + rule + " applied.", lineNumber, data);
      this.rule = rule;
    }
  }
}
