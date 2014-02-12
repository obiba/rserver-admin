/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@SuppressWarnings("MagicNumber")
public class ApplicationProperties {

  private int serverPort = 6312;

  private String rExec = "/usr/bin/R";

  private int rServePort = 6311;

  private String rServeEncoding = "utf8";

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public String getrExec() {
    return rExec;
  }

  public void setrExec(String rExec) {
    this.rExec = rExec;
  }

  public int getrServePort() {
    return rServePort;
  }

  public void setrServePort(int rServePort) {
    this.rServePort = rServePort;
  }

  public String getrServeEncoding() {
    return rServeEncoding;
  }

  public void setrServeEncoding(String rServeEncoding) {
    this.rServeEncoding = rServeEncoding;
  }
}
