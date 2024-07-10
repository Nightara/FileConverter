package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import lombok.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper=true)
public class SumRule<I> extends SimpleRule<I, I>
{
  private I stash;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<SumRule<I>> ruleGroup;

  public SumRule(String inLabel, String outLabel, Configuration.DataType<I> inType, I defaultVal)
  {
    super(inLabel, outLabel, inType, inType, Mode.SUM, defaultVal,null);
    ruleGroup = new LinkedList<>();
    ruleGroup.add(this);
  }

  public void addRule(SumRule<I> newRule)
  {
    getRuleGroup().add(newRule);
    getRuleGroup().stream()
        .filter(rule -> rule != this)
        .forEach(rule -> rule.setRuleGroup(this.getRuleGroup()));
  }

  @Override
  public boolean canApply(I value)
  {
    return value == null || value instanceof Number || value instanceof Temporal || value instanceof String;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<I> apply(I value)
  {
    if(value == null)
    {
      value = getDefaultVal();
    }
    setStash(value);

    Result<I> result = null;
    if(isGroupFilled())
    {
      if(value instanceof Number)
      {
        double sum = ((Number) getDefaultVal()).doubleValue() + getRuleGroup().stream()
            .map(SumRule::getStash)
            .map(Number.class::cast)
            .mapToDouble(Number::doubleValue)
            .sum();

        I parsedSum;
        if(value instanceof Integer)
        {
          parsedSum = (I) Integer.valueOf((int) sum);
        }
        else if(value instanceof Double)
        {
          parsedSum = (I) Double.valueOf(sum);
        }
        else
        {
          return null;
        }

        result = new Result<>(getOutLabel(),this, parsedSum);
      }
      else if(value instanceof Temporal)
      {
        Temporal tempDefaultVal = (Temporal) getDefaultVal();
        result = new Result<>(getOutLabel(),this, (I) tempDefaultVal.plus(getRuleGroup().stream()
            .map(SumRule::getStash)
            .map(Temporal.class::cast)
            .map(temporal -> Duration.between(Instant.EPOCH, temporal))
            .reduce(Duration.ZERO, Duration::plus)));
      }
      else if(value instanceof String)
      {
        String stringDefaultVal = (String) getDefaultVal();
        result = new Result<>(getOutLabel(),this, (I) getRuleGroup().stream()
            .map(SumRule::getStash)
            .map(String.class::cast)
            .reduce(stringDefaultVal, String::concat));
      }

      getRuleGroup().forEach(SumRule::clearStash);
    }
    return result;
  }

  @Override
  public boolean canReverse(I value)
  {
    return false;
  }

  @Override
  public Result<I> reverse(I value)
  {
    return null;
  }

  public boolean hasStash()
  {
    return getStash() != null;
  }

  private void clearStash()
  {
    setStash(null);
  }

  public boolean isGroupFilled()
  {
    return getRuleGroup().stream()
        .map(SumRule::hasStash)
        .reduce(this.hasStash(), Boolean::logicalAnd);
  }

  public I getDefaultVal()
  {
    return getInVal();
  }
}
