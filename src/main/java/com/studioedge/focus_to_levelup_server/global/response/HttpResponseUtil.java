package com.studioedge.focus_to_levelup_server.global.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class HttpResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setSuccessResponse(HttpServletResponse response, HttpStatus httpStatus, Object body)
            throws IOException {
        String responseBody = objectMapper.writeValueAsString(CommonResponse.ok(body));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }

    public static void setErrorResponse(HttpServletResponse response, HttpStatus httpStatus, Object body)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    // REST API용 메서드들
    public static <T> ResponseEntity<CommonResponse<T>> ok(T data) {
        return ResponseEntity.ok(CommonResponse.ok(data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> updated(T data) {
        return ResponseEntity.ok(CommonResponse.ok(data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> delete(T data) {
        return ResponseEntity.ok(CommonResponse.ok(data));
    }
}
