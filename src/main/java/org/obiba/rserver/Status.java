package org.obiba.rserver;

public class Status {

  private final boolean running;

  private final Integer port;

  private final String encoding;

  public Status(RService rService) {
    running = rService.isRunning();
    port = rService.getPort();
    encoding = rService.getEncoding();
  }

  public boolean isRunning() {
    return running;
  }

  public Integer getPort() {
    return port;
  }

  public String getEncoding() {
    return encoding;
  }
}