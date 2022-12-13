package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

import java.util.regex.*;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper=true)
public class RegexRule<O> extends Rule<String, O>
{
  public static final String MULTI_REGEX_SEPARATOR = "/";

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
          try
          {
            return new Result<>(getOutLabel(),this, (O) m.group(Integer.parseInt(getOutVal().toString())));
          }
          catch(NumberFormatException ex)
          {
            return new Result<>(getOutLabel(),this, (O) m.group(getOutVal().toString()));
          }
          catch(IndexOutOfBoundsException | IllegalArgumentException ex)
          {
            return null;
          }
        case REGEX_MULTI:
          String[] groups = getOutVal().toString().split(MULTI_REGEX_SEPARATOR);
          StringBuilder result = new StringBuilder();
          for(String group : groups)
          {
            try
            {
              result.append(m.group(Integer.parseInt(group)));
            }
            catch(NumberFormatException ex)
            {
              result.append(m.group(group));
            }
            catch(IndexOutOfBoundsException | IllegalArgumentException ex)
            {
              return null;
            }
          }
          return new Result<>(getOutLabel(),this, (O) result.toString());
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
