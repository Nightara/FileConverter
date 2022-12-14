package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import lombok.*;

@EqualsAndHashCode(callSuper=true)
public class FilterRule<I, O> extends Rule<I, O>
{
  @Getter
  Rule<I, O> innerRule;

  public FilterRule(Rule<I, O> innerRule)
  {
    super(innerRule.getInLabel(), innerRule.getInLabel(), innerRule.getInType(), innerRule.getOutType(), Mode.FILTER,
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
}
