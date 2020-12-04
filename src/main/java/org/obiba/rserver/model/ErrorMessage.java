package org.obiba.rserver.model;

public interface ErrorMessage {

    String getStatus();

    String getKey();

    String[] getArgs();

    String getMessage();

}
