package de.dkfz.sbst.tichawa.util.converter.parser;

import lombok.*;

import java.io.*;

@Getter
@EqualsAndHashCode(callSuper=true)
public class ParseException extends IllegalArgumentException
{
  private final int lineNumber;
  private final Serializable data;

  public ParseException(String s, int lineNumber, Serializable data)
  {
    super(s);
    this.data = data;
    this.lineNumber = lineNumber;
  }

  @Override
  public String getMessage()
  {
    return super.getMessage() + " in line " + getLineNumber() + ".";
  }
}
