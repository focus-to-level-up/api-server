package com.studioedge.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class AdminExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception exception, Model model) {
        log.error("Unexpected admin request failure", exception);
        model.addAttribute("message", "요청을 처리하는 중 문제가 발생했습니다.");
        return "error/500";
    }
}
