package org.obiba.rserver.rest;


import com.google.common.collect.Lists;
import org.obiba.rserver.domain.RServeSession;
import org.obiba.rserver.model.RCommand;
import org.obiba.rserver.model.RSession;
import org.obiba.rserver.r.StringAssignROperation;
import org.obiba.rserver.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class RSessionController {

    @Autowired
    private RSessionService rSessionService;

    @GetMapping("/r/session/{id}")
    RSession getSession(@PathVariable String id) {
        return rSessionService.getRSession(id);
    }

    @DeleteMapping("/r/session/{id}")
    ResponseEntity<?> deleteSession(@PathVariable String id) {
        rSessionService.closeRSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/r/session/{id}/_assign", consumes = "text/plain")
    ResponseEntity<RCommand> assignValue(@PathVariable String id, @RequestParam(name = "s") String symbol,
                                  @RequestParam(name = "async", defaultValue = "false") boolean async,
                                  @RequestBody String value, UriComponentsBuilder ucb) {
        RServeSession rSession = rSessionService.getRServeSession(id);
        StringAssignROperation rop = new StringAssignROperation(symbol, value);
        if (async) {
            String rCommandId = rSession.executeAsync(rop);
            RCommand rCommand = rSession.getRCommand(rCommandId);
            return ResponseEntity.created(ucb.path("/r/session/{id}/command/{rid}").buildAndExpand(rSession.getId(), rCommandId).toUri())
                    .body(rCommand);
        } else {
            rSession.execute(rop);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/r/session/{id}/commands")
    List<RCommand> getCommands(@PathVariable String id) {
        return StreamSupport.stream(rSessionService.getRServeSession(id).getRCommands().spliterator(), false)
                .map(c -> (RCommand)c).collect(Collectors.toList());
    }

    @GetMapping("/r/session/{id}/command/{cmdId}")
    RCommand getCommand(@PathVariable String id, @PathVariable String cmdId) {
        return rSessionService.getRServeSession(id).getRCommand(cmdId);
    }

    @DeleteMapping("/r/session/{id}/command/{cmdId}")
    RCommand deleteCommand(@PathVariable String id, @PathVariable String cmdId) {
        return rSessionService.getRServeSession(id).removeRCommand(cmdId);
    }

}
