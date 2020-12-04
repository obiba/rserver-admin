package org.obiba.rserver.rest;


import org.obiba.rserver.model.RSession;
import org.obiba.rserver.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/r/sessions")
public class RSessionsController {

    @Autowired
    private RSessionService rSessionService;

    @PostMapping
    ResponseEntity<?> createRSession(UriComponentsBuilder ucb) {
        RSession rSession = rSessionService.createRSession();
        return ResponseEntity.created(ucb.path("/r/session/{id}").buildAndExpand(rSession.getId()).toUri()).body(rSession);
    }

}
