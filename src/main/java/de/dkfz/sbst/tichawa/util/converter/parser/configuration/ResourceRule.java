package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import lombok.*;
import lombok.experimental.Delegate;
import org.apache.poi.ss.formula.eval.*;

public class ResourceRule<I, O> extends Rule<I, O>
{
  Rule<I, O> innerRule;

  public ResourceRule(Rule<I, O> innerRule)
  {
    super(innerRule.getInLabel(), innerRule.getOutLabel(), innerRule.getInType(), innerRule.getOutType(),
        innerRule.getMode(), innerRule.getInVal(), innerRule.getOutVal());
    this.innerRule = innerRule;
  }

  @Override
  public boolean canApply(I value)
  {
    throw new NotImplementedException("Not implemented yet.");
  }

  @Override
  public Result<O> apply(I value)
  {
    throw new NotImplementedException("Not implemented yet.");
  }

  @Override
  public boolean canReverse(O value)
  {
    throw new NotImplementedException("Not implemented yet.");
  }

  @Override
  public Result<I> reverse(O value)
  {
    throw new NotImplementedException("Not implemented yet.");
  }
}
