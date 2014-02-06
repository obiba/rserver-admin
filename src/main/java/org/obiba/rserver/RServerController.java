package org.obiba.rserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rserver")
public class RServerController {

  @Autowired
  private RService rService;

  @RequestMapping(value = "/status", method = RequestMethod.GET)
  public
  @ResponseBody
  Status getStatus(@RequestParam(value = "name", required = false, defaultValue = "Stranger") String name) {
    return new Status(rService.isRunning());
  }

}