package com.studioedge.focus_to_levelup_server.global.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {
        // TODO: 도메인별 예외 등록
        // setUpAuthException();
        // setUpUserException();
    }

    public static ExceptionSituation getSituationOf(Exception exception) {
        return mapper.get(exception.getClass());
    }

    // Example: Auth 관련 예외 등록
    // private static void setUpAuthException() {
    //     mapper.put(UserNotFoundException.class,
    //             ExceptionSituation.of("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));
    // }
}
