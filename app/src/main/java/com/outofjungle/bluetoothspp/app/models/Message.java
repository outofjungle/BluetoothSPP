package com.outofjungle.bluetoothspp.app.models;

/**
 * Created by vvenkat on 6/3/14.
 */

public class Message {
  protected String text;
  protected Writer writer;

  public Message(String text, Writer writer) {
    this.text = text;
    this.writer = writer;
  }

  public String getText() {
    return text;
  }

  public Enum<Writer> getWriter() {
    return writer;
  }

  public boolean isPhone() {
    return Writer.ANDROID == writer;
  }

  public boolean isArduino() {
    return Writer.ARDUINO == writer;
  }
}
