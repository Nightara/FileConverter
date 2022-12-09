package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

import java.util.*;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Rule<I, O>
{
  @NonNull
  private String inLabel;
  @NonNull
  private String outLabel;
  @NonNull
  private DataType<I> inType;
  @NonNull
  private DataType<O> outType;
  @NonNull
  private Rule.Mode mode;
  private I inVal;
  private O outVal;

  public abstract boolean canApply(I value);

  public abstract Result<O> apply(I value);

  public abstract boolean canReverse(O value);

  public abstract Result<I> reverse(O value);

  public Optional<Result<O>> tryApply(I value)
  {
    try
    {
      return Optional.ofNullable(apply(value));
    }
    catch(Exception ex)
    {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unused")
  public Optional<Result<I>> tryReverse(O value)
  {
    try
    {
      return Optional.ofNullable(reverse(value));
    }
    catch(Exception ex)
    {
      return Optional.empty();
    }
  }

  public enum Mode
  {
    KEEP, TRANSLATE, STATIC, REGEX, SUM, SPECIAL;

    public static Optional<Mode> getMode(String name)
    {
      return Arrays.stream(Mode.values())
          .filter(mode -> mode.name().equals(name))
          .findAny();
    }
  }

  public static <I, O> Rule<O, I> reverse(Rule<I, O> rule)
  {
    return new Rule.Reverse<>(rule);
  }

  @Value
  public static class Result<K>
  {
    String label;
    Rule<?, K> rule;
    K data;
  }

  private static class Reverse<O, I> extends Rule<O, I>
  {
    @Getter(AccessLevel.PRIVATE)
    private final Rule<I, O> innerRule;

    public Reverse(Rule<I, O> innerRule)
    {
      super(innerRule.getOutLabel(), innerRule.getInLabel(), innerRule.getOutType(), innerRule.getInType(),
          innerRule.getMode(), innerRule.getOutVal(), innerRule.getInVal());
      this.innerRule = innerRule;
    }

    @Override
    public boolean canApply(O value)
    {
      return getInnerRule().canReverse(value);
    }

    @Override
    public Result<I> apply(O value)
    {
      return getInnerRule().reverse(value);
    }

    @Override
    public boolean canReverse(I value)
    {
      return getInnerRule().canApply(value);
    }

    @Override
    public Result<O> reverse(I value)
    {
      return getInnerRule().apply(value);
    }
  }
}
