package org.obiba.rserver.rest;

import org.obiba.rserver.domain.ExceptionErrorMessage;
import org.obiba.rserver.model.ErrorMessage;
import org.obiba.rserver.r.NoSuchRCommandException;
import org.obiba.rserver.r.REvaluationRuntimeException;
import org.obiba.rserver.r.RRuntimeException;
import org.obiba.rserver.service.RSessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class RSessionAdvice {

    @ResponseBody
    @ExceptionHandler(RSessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorMessage rSessionNotFoundHandler(RSessionNotFoundException ex) {
        return new ExceptionErrorMessage(HttpStatus.NOT_FOUND, ex, ex.getId());
    }

    @ResponseBody
    @ExceptionHandler(NoSuchRCommandException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorMessage rCommandNotFoundHandler(NoSuchRCommandException ex) {
        return new ExceptionErrorMessage(HttpStatus.NOT_FOUND, ex, ex.getId());
    }

    @ResponseBody
    @ExceptionHandler(RRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorMessage rRuntimeHandler(RRuntimeException ex) {
        return new ExceptionErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorMessage illegalArgumentHandler(IllegalArgumentException ex) {
        return new ExceptionErrorMessage(HttpStatus.BAD_REQUEST, ex);
    }

    @ResponseBody
    @ExceptionHandler(REvaluationRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorMessage illegalArgumentHandler(REvaluationRuntimeException ex) {
        return new ExceptionErrorMessage(HttpStatus.BAD_REQUEST, ex);
    }

}
