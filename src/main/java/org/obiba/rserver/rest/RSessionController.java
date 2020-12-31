package org.obiba.rserver.rest;


import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.obiba.rserver.domain.RServeCommand;
import org.obiba.rserver.domain.RServeSession;
import org.obiba.rserver.model.RCommand;
import org.obiba.rserver.model.RSession;
import org.obiba.rserver.r.*;
import org.obiba.rserver.service.RSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
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

    @PostMapping(value = "/r/session/{id}/_assign", consumes = "application/x-rscript")
    ResponseEntity<RCommand> assignScript(@PathVariable String id, @RequestParam(name = "s") String symbol,
                                          @RequestParam(name = "async", defaultValue = "false") boolean async,
                                          @RequestBody String script, UriComponentsBuilder ucb) {
        RScriptROperation rop = new RScriptAssignROperation(String.format("base::assign('%s', %s)", symbol, script));
        return doAssign(id, rop, async, ucb);
    }

    @PostMapping(value = "/r/session/{id}/_eval", consumes = "application/x-rscript")
    ResponseEntity<?> evalScript(@PathVariable String id,
                                 @RequestParam(name = "async", defaultValue = "false") boolean async,
                                 @RequestBody String script, UriComponentsBuilder ucb) {
        RScriptROperation rop = new RScriptROperation(script);
        return doEval(id, rop, async, ucb);
    }

    //
    // File transfers
    //

    @PostMapping(value = "/r/session/{id}/_upload", consumes = "multipart/form-data")
    ResponseEntity<?> uploadFile(@PathVariable String id,
                                 @RequestParam("file") CommonsMultipartFile file,
                                 @RequestParam(value = "path", required = false) String path,
                                 @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
                                 @RequestParam(value = "temp", required = false, defaultValue = "false") boolean temp) {
        RServeSession rServeSession = getRServeSession(id);
        String destinationPath = Strings.isNullOrEmpty(path) ? file.getOriginalFilename() : path;
        try {
            doWriteFile(file.getInputStream(), temp ? rServeSession.getTempDir() : rServeSession.getWorkDir(), destinationPath, overwrite);
        } catch (IOException e) {
            throw new RRuntimeException("File write failed");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/r/session/{id}/_download")
    void downloadFile(HttpServletResponse response,
                      @PathVariable String id,
                      @RequestParam(value = "path", required = false) String path,
                      @RequestParam(value = "temp", required = false, defaultValue = "false") boolean temp) {
        RServeSession rServeSession = getRServeSession(id);
        File sourceFile = new File(temp ? rServeSession.getTempDir() : rServeSession.getWorkDir(), path);
        String mimeType = URLConnection.guessContentTypeFromName(sourceFile.getName());
        if (Strings.isNullOrEmpty(mimeType)) {
            try (InputStream in = new FileInputStream(sourceFile)) {
                mimeType = URLConnection.guessContentTypeFromStream(in);
            } catch (IOException e) {
                // ignore
            }
        }
        if (Strings.isNullOrEmpty(mimeType)) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        //response.setContentLength((int) sourceFile.length());
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", sourceFile.getName());
        response.setHeader(headerKey, headerValue);

        // write bytes read from the input stream into the output stream
        try {
            // copies all bytes from a file to an output stream
            Files.copy(sourceFile, response.getOutputStream());
            // flushes output stream
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new RRuntimeException("File read failed");
        }
    }

    //
    // Commands management
    //

    @GetMapping("/r/session/{id}/commands")
    List<RCommand> getCommands(@PathVariable String id) {
        return StreamSupport.stream(getRServeSession(id).getRCommands().spliterator(), false)
                .map(c -> (RCommand) c).collect(Collectors.toList());
    }

    @GetMapping("/r/session/{id}/command/{cmdId}")
    RCommand getCommand(@PathVariable String id, @PathVariable String cmdId) {
        return getRServeSession(id).getRCommand(cmdId);
    }

    @DeleteMapping("/r/session/{id}/command/{cmdId}")
    RCommand deleteCommand(@PathVariable String id, @PathVariable String cmdId) {
        return getRServeSession(id).removeRCommand(cmdId);
    }

    @GetMapping(value = "/r/session/{id}/command/{cmdId}/result", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<?> getCommandResult(@PathVariable String id, @PathVariable String cmdId,
                                       @RequestParam(name = "wait", defaultValue = "false") boolean wait,
                                       @RequestParam(name = "rm", defaultValue = "true") boolean remove) {
        RServeSession rSession = getRServeSession(id);
        RServeCommand rCommand = rSession.getRCommand(cmdId);
        ResponseEntity<?> noContent = ResponseEntity.noContent().build();
        if (!rCommand.isFinished()) {
            if (wait) {
                try {
                    synchronized (rCommand) {
                        rCommand.wait();
                    }
                } catch (InterruptedException e) {
                    return noContent;
                }
            } else {
                return noContent;
            }
        }
        return getFinishedRCommandResult(rSession, rCommand, remove);
    }

    //
    // Private methods
    //

    private ResponseEntity<RCommand> doAssign(String id, ROperation rop, boolean async, UriComponentsBuilder ucb) {
        RServeSession rSession = getRServeSession(id);
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

    private ResponseEntity<?> doEval(String id, ROperationWithResult rop, boolean async, UriComponentsBuilder ucb) {
        RServeSession rSession = getRServeSession(id);
        if (async) {
            String rCommandId = rSession.executeAsync(rop);
            RCommand rCommand = rSession.getRCommand(rCommandId);
            return ResponseEntity.created(ucb.path("/r/session/{id}/command/{rid}").buildAndExpand(rSession.getId(), rCommandId).toUri())
                    .body(rCommand);
        } else {
            rSession.execute(rop);
            if (rop.hasResult() && rop.hasRawResult()) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rop.getRawResult().asBytes());
            }
            throw new RRuntimeException("No eval result could be extracted");
        }
    }

    private void doWriteFile(InputStream in, String rootFolder, String destinationPath, boolean overwrite) throws IOException {
        File outFile = new File(rootFolder, destinationPath);
        if (outFile.exists() && !overwrite) {
            throw new IllegalArgumentException("File exists and cannot be overridden");
        }
        if (!outFile.getParentFile().exists()) {
            if (!outFile.getParentFile().mkdirs()) {
                throw new IllegalArgumentException("File parent folder cannot be created");
            }
        }

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            ByteStreams.copy(in, out);
        }
    }

    private ResponseEntity<?> getFinishedRCommandResult(RServeSession rSession, RServeCommand rCommand, boolean remove) {
        ResponseEntity<?> resp = ResponseEntity.noContent().build();
        if (rCommand.isWithResult()) {
            ROperationWithResult rop = rCommand.asROperationWithResult();
            if (rop.hasRawResult()) {
                resp = ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rop.getRawResult().asBytes());
            }
        }
        if (remove) rSession.removeRCommand(rCommand.getId());
        return resp;
    }

    private RServeSession getRServeSession(String id) {
        // TODO authenticate and check session's belongs to subject
        return rSessionService.getRServeSession(id);
    }

}
