package org.obiba.rserver.rest;


import org.obiba.rserver.model.RSession;
import org.obiba.rserver.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/r/sessions")
public class RSessionsController {

    @Autowired
    private RSessionService rSessionService;

    @GetMapping
    List<RSession> getRSessions() {
        // TODO requires admin role
        return rSessionService.getRSessions();
    }

    @PostMapping
    ResponseEntity<?> createRSession(@RequestParam(name = "subject") String subject, UriComponentsBuilder ucb) {
        RSession rSession = rSessionService.createRSession(subject);
        return ResponseEntity.created(ucb.path("/r/session/{id}").buildAndExpand(rSession.getId()).toUri()).body(rSession);
    }

}
