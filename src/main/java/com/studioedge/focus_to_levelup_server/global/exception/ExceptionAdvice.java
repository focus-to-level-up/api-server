package com.studioedge.focus_to_levelup_server.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ExceptionAdvice {
    Logger defaultLog = LoggerFactory.getLogger(ExceptionAdvice.class);
    Logger exceptionLog = LoggerFactory.getLogger("ExceptionLogger");

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ExceptionResponse> handleCommonException(CommonException e) {
        ExceptionSituation exceptionSituation = ExceptionMapper.getSituationOf(e);

        defaultLog.warn(exceptionSituation.getMessage());
        exceptionLog.warn(exceptionSituation.getMessage(), e);

        return ResponseEntity.status(exceptionSituation.getStatusCode())
                .body(ExceptionResponse.from(exceptionSituation));
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            ServletRequestBindingException.class,
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            NoHandlerFoundException.class,
            NoResourceFoundException.class,
            AsyncRequestTimeoutException.class,
            ErrorResponseException.class,
            MaxUploadSizeExceededException.class,
            ConversionNotSupportedException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            MethodValidationException.class,
            BindException.class,
    })
    public ResponseEntity<ExceptionResponse> handleValidationException(Exception ex) {
        String message = ex.getMessage();

        if (ex instanceof MethodArgumentNotValidException e) {
            message = e.getBindingResult().getFieldError().getDefaultMessage();
        }

        defaultLog.warn("Validation failed: {}", message);
        exceptionLog.warn("Validation failed", ex);

        ExceptionSituation situation = ExceptionSituation.of(message, HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest()
                .body(ExceptionResponse.from(situation));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        if (e instanceof NoResourceFoundException) {
            defaultLog.info(e.getMessage());
            ExceptionSituation situation = ExceptionSituation.of(e.getMessage(), HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ExceptionResponse.from(situation));
        }

        defaultLog.error(e.getMessage());
        exceptionLog.error(e.getMessage(), e);

        ExceptionSituation situation = ExceptionSituation.of(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError()
                .body(ExceptionResponse.from(situation));
    }
}
