package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

import java.util.regex.*;

/**
 * A RegexRule represents a rule matching a regular expression given as inVal to the value it is applied to, and then
 * proceeds differently based on its mode:
 * - Regex tries to match the regular expression to the provided value, and then returns the group identified by the
 * number provided in outVal.
 * - RegexMulti works like Regex, but takes a slash-separated list of group numbers, and returns a concatenation of the
 * specified groups.
 * - RegexReplace replaces all matches of the supplied regular expression with the value provided in outVal.
 * - RegexTranslate works like Translate, but applies to any values matching the given regular expression.
 *
 * @param <O> The output type
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper=true)
public class RegexRule<O> extends Rule<String, O>
{
  public static final String MULTI_REGEX_SEPARATOR = "/";

  private final Pattern pattern;

  public RegexRule(String inLabel, String outLabel, DataType<O> outType, Mode mode, String pattern, O outVal)
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
      return switch(getMode())
      {
        case REGEX ->
        {
          try
          {
            yield new Result<>(getOutLabel(), this, (O) m.group(Integer.parseInt(getOutVal().toString())));
          }
          catch(NumberFormatException ex)
          {
            yield new Result<>(getOutLabel(), this, (O) m.group(getOutVal().toString()));
          }
          catch(IndexOutOfBoundsException | IllegalArgumentException ex)
          {
            yield null;
          }
        }
        case REGEX_MULTI ->
        {
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
              yield null;
            }
          }
          yield new Result<>(getOutLabel(), this, (O) result.toString());
        }
        case REGEX_REPLACE -> new Result<>(getOutLabel(), this, (O) m.replaceAll(getOutVal().toString()));
        case REGEX_TRANSLATE -> new Result<>(getOutLabel(), this, getOutVal());
        default -> null;
      };
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
