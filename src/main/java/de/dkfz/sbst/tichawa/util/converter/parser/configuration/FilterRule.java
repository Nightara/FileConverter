package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import lombok.*;

/**
 * A FilterRule is a special rule that is not meant to produce values, but is instead meant to be used as a filter to
 * completely remove an entire line of input under certain circumstances.
 * A FilterRule always contains a sub-rule, and if the sub-rule applies to the given input, the current line should be
 * discarded.
 *
 * Regular rules can be converted into FilterRules in a config file by prepending the mode with "FILTER/".
 * The sub-rule will be parsed as normal using the config file (But without the "FILTER/" prefix in its mode), and then
 * a new FilterRule will be created containing this sub-rule.
 *
 * @param <I> The input type
 * @param <O> The output type
 */
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
  @SuppressWarnings({"java:S2166", "java:S110"})
  public static class FilterException extends ParseException
  {
    private final transient Rule<?, ?> rule;

    public FilterException(Rule<?, ?> rule, int lineNumber, Object data)
    {
      super("FilterRule " + rule + " applied.", lineNumber, data);
      this.rule = rule;
    }
  }
}
