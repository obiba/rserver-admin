package org.obiba.rserver.rest;


import org.obiba.rserver.model.RSession;
import org.obiba.rserver.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RSessionController {

    @Autowired
    private RSessionService rSessionService;

    @GetMapping("/r/session/{id}")
    RSession get(@PathVariable String id) {
        return rSessionService.getRSession(id);
    }

    @DeleteMapping("/r/session/{id}")
    ResponseEntity<?> delete(@PathVariable String id) {
        rSessionService.closeRSession(id);
        return ResponseEntity.noContent().build();
    }

}
