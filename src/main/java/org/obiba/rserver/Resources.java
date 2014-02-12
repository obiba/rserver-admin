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

import java.io.File;

public class Resources {

  @SuppressWarnings("StaticNonFinalField")
  private static File rServerHomeFile;

  private Resources() {}

  public static File getRServerHomeDir() {
    if(rServerHomeFile == null) {
      if(System.getenv().containsKey("RSERVER_HOME")) {
        rServerHomeFile = new File(System.getenv("RSERVER_HOME"));
      } else if(System.getProperties().containsKey("RSERVER_HOME")) {
        rServerHomeFile = new File(System.getProperty("RSERVER_HOME"));
      } else {
        throw new IllegalStateException("Cannot find RSERVER_HOME environment variable or system property");
      }
    }
    return rServerHomeFile;
  }
}
