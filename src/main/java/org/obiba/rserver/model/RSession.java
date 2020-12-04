package org.obiba.rserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public interface RSession {

    String getId();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    Date getCreated();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    Date getLastAccess();

    boolean isBusy();

    void close();
}
