package org.obiba.rserver;

public class Status {

  private final boolean running;

  public Status(boolean running) {
    this.running = running;
  }

  public boolean isRunning() {
    return running;
  }
}