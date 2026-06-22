package com.studioedge.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.domain.auth.exception.WithdrawnMemberException;
import com.studioedge.global.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        Object exception = request.getAttribute("exception");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ExceptionResponse errorResponse;

        if (exception instanceof WithdrawnMemberException withdrawnMemberException) {
            log.error("WithdrawnMemberException detected in EntryPoint");
            response.setStatus(withdrawnMemberException.getStatus());
            errorResponse = ExceptionResponse.of(withdrawnMemberException.getStatus(), withdrawnMemberException.getMessage());
        } else {
            log.error("Unauthorized error: {}", authException.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorResponse = ExceptionResponse.of(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
