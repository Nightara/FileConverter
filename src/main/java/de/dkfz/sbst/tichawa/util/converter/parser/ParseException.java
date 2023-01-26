package de.dkfz.sbst.tichawa.util.converter.parser;

import lombok.*;

import java.io.*;

@Value
@EqualsAndHashCode(callSuper=true)
public class ParseException extends IllegalArgumentException
{
  Serializable data;

  public ParseException(String s, Serializable data)
  {
    super(s);
    this.data = data;
  }
}
