/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rserver.service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.obiba.rserver.RProperties;
import org.obiba.rserver.Resources;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Service to manage RServer process.
 */
@Component
public class RServerService implements RServerState {

  private static final Logger log = LoggerFactory.getLogger(RServerService.class);

  @Autowired
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private RProperties properties;

  private int rserveStatus = -1;

  @Override
  public Integer getPort() {
    return properties.getServerPort();
  }

  @Override
  public String getEncoding() {
    return properties.getServerEncoding();
  }

  @Override
  public boolean isRunning() {
    return rserveStatus == 0;
  }

  @PostConstruct
  public void start() {

    if(rserveStatus == 0) {
      log.error("RServerService is already running");
      return;
    }

    log.info("Start RServerService with {}", properties);

    // fresh start, try to kill any remains of R server
    try {
      newRConnection().shutdown();
    } catch(Exception e) {
      // ignore
    }

    try {
      // launch the Rserve daemon and wait for it to complete
      Process rserve = buildRProcess().start();
      rserveStatus = rserve.waitFor();
      if(rserveStatus == 0) {
        log.info("R server started");
      } else {
        log.error("R server start failed with status: {}", rserveStatus);
        rserveStatus = -1;
      }
    } catch(Exception e) {
      log.warn("R server start failed", e);
      rserveStatus = -1;
    }
  }

  @PreDestroy
  public void stop() {
    if(rserveStatus != 0) return;

    try {
      log.info("Shutting down R server...");
      newConnection().shutdown();
      log.info("R server shut down");
      File workDir = getWorkingDirectory();
      for (File file : workDir.listFiles()) {
        delete(file);
      }
    } catch(Exception e) {
      log.error("R server shutdown failed", e);
    }
    rserveStatus = -1;
  }

  /**
   * Creates a new connection to R server.
   *
   * @return
   */
  public RConnection newConnection() {
    try {
      return newRConnection();
    } catch(RserveException e) {
      log.error("Error while connecting to R: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  //
  // Private methods
  //

  /**
   * Create a new RConnection given the R server settings.
   *
   * @return
   * @throws RserveException
   */
  private RConnection newRConnection() throws RserveException {
    RConnection conn = new RConnection();

//    if(conn.needLogin()) {
//      conn.login(username, password);
//    }

    if(properties.getServerEncoding() != null) {
      conn.setStringEncoding(properties.getServerEncoding());
    }

    return conn;
  }

  private ProcessBuilder buildRProcess() {
    List<String> args = getArguments();
    log.info("Starting R server: {}", StringUtils.collectionToDelimitedString(args, " "));
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(getWorkingDirectory());
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRserveLog()));
    return pb;
  }

  private List<String> getArguments() {
    List<String> args = Lists.newArrayList(properties.getExec(), "CMD", "Rserve", "--vanilla");
    if(properties.getServerPort() > 0) {
      args.add("--RS-port");
      args.add(String.valueOf(properties.getServerPort()));
    }
    if(!Strings.isNullOrEmpty(properties.getServerEncoding())) {
      args.add("--RS-encoding");
      args.add(properties.getServerEncoding());
    }
    File workDir = getWorkingDirectory();
    args.add("--RS-workdir");
    args.add(workDir.getAbsolutePath());

    File conf = getRservConf();
    if(conf.exists()) {
      args.add("--RS-conf");
      args.add(conf.getAbsolutePath());
    }

    return args;
  }

  private void delete(File file) {
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        delete(f);
      }
    }
    if (!file.isDirectory() || file.list().length == 0) {
      if (!file.delete()) {
        log.warn("Unable to delete file: " + file.getAbsolutePath());
      }
    }
  }



  private File getWorkingDirectory() {
    File dir = new File(Resources.getRServerHomeDir(), "work" + File.separator + "R");
    if(!dir.exists()) {
      if(!dir.mkdirs()) {
        log.error("Unable to create: {}", dir.getAbsolutePath());
      }
    }
    return dir;
  }

  private File getRserveLog() {
    File logFile = new File(Resources.getRServerHomeDir(), "logs" + File.separator + "Rserve.log");
    if(!logFile.getParentFile().exists()) {
      if(!logFile.getParentFile().mkdirs()) {
        log.error("Unable to create: {}", logFile.getParentFile().getAbsolutePath());
      }
    }
    return logFile;
  }

  private File getRservConf() {
    return new File(Resources.getRServerHomeDir(), "conf" + File.separator + "Rserv.conf");
  }

}
