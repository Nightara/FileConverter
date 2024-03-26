package de.dkfz.sbst.tichawa.util.converter.parser;

import lombok.*;

@Getter
@EqualsAndHashCode(callSuper=true)
public class ParseException extends IllegalArgumentException
{
  private final int lineNumber;
  private final transient Object data;

  public ParseException(String s, int lineNumber, Object data)
  {
    super(s);
    this.data = data;
    this.lineNumber = lineNumber;
  }

  public <T> T getData()
  {
    //noinspection unchecked
    return (T) data;
  }

  @Override
  public String getMessage()
  {
    return super.getMessage() + " in line " + getLineNumber() + ".";
  }
}
