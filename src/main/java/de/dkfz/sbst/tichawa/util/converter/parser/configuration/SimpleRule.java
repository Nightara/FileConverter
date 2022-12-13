package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

@Getter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper=true)
public class SimpleRule<I, O> extends Rule<I, O>
{
  final Pattern pattern;

  public SimpleRule(String inLabel, String outLabel, DataType<I> inType, DataType<O> outType, Mode mode, I inVal, O outVal)
  {
    super(inLabel, outLabel, inType, outType, mode, inVal, outVal);

    if(mode == Mode.REGEX)
    {
      pattern = Pattern.compile(inVal.toString());
    }
    else
    {
      // Empty negative lookahead, always fails to match
      pattern = Pattern.compile("(?!)");
    }
  }

  @Override
  public boolean canApply(I value)
  {
    switch(getMode())
    {
      case KEEP:
        return (value == null && getOutVal() != null)
            || getOutType().getClazz().isInstance(value);
      case TRANSLATE:
        return getInVal().equals(value);
      case STATIC:
        return true;
      case SPECIAL:
        return (getOutType() == DataType.INSTANT && getInVal().equals("NOW"))
            || (getInType() == DataType.INSTANT && getOutType() == DataType.STRING && value instanceof Instant)
            || (getInType() == DataType.INSTANT && getOutType() == DataType.LOCALDATE && getInVal() != null)
            || (getInType() == DataType.INSTANT && getOutType() == DataType.LOCALDATE && value instanceof Instant);
      default:
        return false;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<O> apply(I value)
  {
    switch(getMode())
    {
      case KEEP:
        return new Result<>(getOutLabel(),this, value != null ? (O) value : getOutVal());
      case TRANSLATE:
        return getInVal().equals(value) ? new Result<>(getOutLabel(),this, getOutVal()) : null;
      case STATIC:
        return new Result<>(getOutLabel(),this, getOutVal());
      case SPECIAL:
        return applySpecialMode(value);
      default:
        return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Result<O> applySpecialMode(I value)
  {
    if(getOutType() == DataType.INSTANT && getInVal().equals("NOW"))
    {
      return new Result<>(getOutLabel(),this, (O) Instant.now());
    }
    else if(getInType() == DataType.INSTANT && getOutType() == DataType.LOCALDATE)
    {
      return Optional.ofNullable(value)
          .or(() -> Optional.ofNullable(getInVal()))
          .map(Instant.class::cast)
          .map(val -> val.atZone(ZoneOffset.UTC))
          .map(ZonedDateTime::toLocalDate)
          .map(val -> new Result<>(getOutLabel(),this, (O) val))
          .orElse(null);
    }
    else if(getInType() == DataType.INSTANT && getOutType() == DataType.STRING)
    {
      try
      {
        return new Result<>(getOutLabel(),this, (O) DateTimeFormatter.ofPattern(getOutVal().toString())
            .format(((Instant) value).atOffset(ZoneOffset.UTC)));
      }
      catch(DateTimeParseException ex)
      {
        return null;
      }
    }
    else
    {
      return null;
    }
  }

  @Override
  public boolean canReverse(O value)
  {
    switch(getMode())
    {
      case KEEP:
        return getInType().getClazz().isInstance(value);
      case TRANSLATE:
        return getOutVal().equals(value);
      case STATIC:
      case SPECIAL:
      default:
        return false;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<I> reverse(O value)
  {
    switch(getMode())
    {
      case KEEP:
        return new Result<>(getInLabel(), Rule.reverse(this), (I) value);
      case TRANSLATE:
        return getOutVal().equals(value) ? new Result<>(getInLabel(), Rule.reverse(this), getInVal()) : null;
      default:
        return null;
    }
  }
}
