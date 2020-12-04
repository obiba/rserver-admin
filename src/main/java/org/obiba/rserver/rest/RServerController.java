/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rserver.rest;

import org.obiba.rserver.service.RServerService;
import org.obiba.rserver.service.RServerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rserver")
public class RServerController {

  @Autowired
  private RServerService rServerService;

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public RServerState getRServerState() {
    return rServerService;
  }

  @RequestMapping(method = RequestMethod.PUT)
  @ResponseBody
  public RServerState start() {
    if(!rServerService.isRunning()) {
      rServerService.start();
    }
    return rServerService;
  }

  @RequestMapping(method = RequestMethod.DELETE)
  @ResponseBody
  public RServerState stop() {
    rServerService.stop();
    return rServerService;
  }

}