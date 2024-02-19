package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class ResourceRule<O> extends Rule<String, O>
{
  private Configuration config;

  public ResourceRule(String inLabel, String outLabel, DataType<O> outType)
  {
    super(inLabel, outLabel, DataType.STRING, outType, Mode.RESOURCE,"",null);
  }

  @Override
  public boolean canApply(String value)
  {
    return getConfig() != null
        && getConfig().resources().containsKey(value)
        && getOutType().getClazz().isInstance(getConfig().resources().get(value));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<O> apply(String value)
  {
    return new Result<>(getOutLabel(),this, (O) getConfig().resources().get(value));
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
