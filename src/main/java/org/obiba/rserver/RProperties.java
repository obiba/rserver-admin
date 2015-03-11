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

import com.google.common.base.Objects;

@ConfigurationProperties(value = "r")
public class RProperties {

  private String exec = "/usr/bin/R";

  public String getExec() {
    return exec;
  }

  public void setExec(String exec) {
    this.exec = exec;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("exec", exec).toString();
  }
}
