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
      pattern = Pattern.compile("(?!)"); // NOSONAR
    }
  }

  @Override
  public boolean canApply(I value)
  {
    return switch(getMode())
    {
      case KEEP -> (value == null && getOutVal() != null)
          || (getOutType() == DataType.INTEGER && value instanceof Number)
          || (getOutType() == DataType.DOUBLE && value instanceof Number)
          || getOutType() == DataType.STRING
          || getOutType().getClazz().isInstance(value);
      case TRANSLATE -> getInVal().equals(value)
          || (getInType() == DataType.INTEGER && value instanceof Number number
              && number.doubleValue() == ((Number) getInVal()).doubleValue());
      case STATIC -> true;
      case SPECIAL -> (getOutType() == DataType.INSTANT && getInVal().equals("NOW"))
          || (getInType() == DataType.INSTANT && getOutType() == DataType.STRING && value instanceof Instant)
          || (getInType() == DataType.INSTANT && getOutType() == DataType.LOCAL_DATE && getInVal() != null)
          || (getInType() == DataType.INSTANT && getOutType() == DataType.LOCAL_DATE && value instanceof Instant);
      default -> false;
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<O> apply(I value)
  {
    return switch(getMode())
        {
          case KEEP -> new Result<>(getOutLabel(), this, value != null ? (O) value : getOutVal());
          case TRANSLATE -> getInVal().equals(value)
              || (getInType() == DataType.INTEGER && value instanceof Number number
                  && number.doubleValue() == ((Number) getInVal()).doubleValue())
              ? new Result<>(getOutLabel(), this, getOutVal()) : null;
          case STATIC -> new Result<>(getOutLabel(), this, getOutVal());
          case SPECIAL -> applySpecialMode(value);
          default -> null;
        };
  }

  @SuppressWarnings("unchecked")
  private Result<O> applySpecialMode(I value)
  {
    if(getOutType() == DataType.INSTANT && getInVal().equals("NOW"))
    {
      return new Result<>(getOutLabel(),this, (O) Instant.now());
    }
    else if(getInType() == DataType.INSTANT && getOutType() == DataType.LOCAL_DATE)
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
  @SuppressWarnings("DuplicateBranchesInSwitch")
  public boolean canReverse(O value)
  {
    return switch(getMode())
    {
      case KEEP -> getInType().getClazz().isInstance(value);
      case TRANSLATE -> getOutVal().equals(value);
      case STATIC, SPECIAL -> false;
      default -> false;
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<I> reverse(O value)
  {
    return switch(getMode())
    {
      case KEEP -> new Result<>(getInLabel(), Rule.reverse(this), (I) value);
      case TRANSLATE ->
          getOutVal().equals(value) ? new Result<>(getInLabel(), Rule.reverse(this), getInVal()) : null;
      default -> null;
    };
  }
}
