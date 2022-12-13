package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

import java.util.regex.*;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper=true)
public class RegexRule<O> extends Rule<String, O>
{
  private final Pattern pattern;

  public RegexRule(@NonNull String inLabel, @NonNull String outLabel, @NonNull DataType<O> outType, @NonNull Mode mode,
                   String pattern, O outVal)
  {
    super(inLabel, outLabel, DataType.STRING, outType, mode, pattern, outVal);
    this.pattern = Pattern.compile(pattern);
  }


  @Override
  public boolean canApply(String value)
  {
    return getPattern().matcher(value).find();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<O> apply(String value)
  {
    Matcher m = getPattern().matcher(value);
    if(m.find())
    {
      switch(getMode())
      {
        case REGEX:
          String outVal = getOutVal().toString();
          try
          {
            return new Result<>(getOutLabel(),this, (O) m.group(Integer.parseInt(outVal)));
          }
          catch(NumberFormatException ex)
          {
            return new Result<>(getOutLabel(),this, (O) m.group(outVal));
          }
          catch(IndexOutOfBoundsException | IllegalArgumentException ex)
          {
            return null;
          }
        case REGEX_TRANSLATE:
          return new Result<>(getOutLabel(),this, getOutVal());
        default:
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
    return false;
  }

  @Override
  public Result<String> reverse(O value)
  {
    return null;
  }
}
