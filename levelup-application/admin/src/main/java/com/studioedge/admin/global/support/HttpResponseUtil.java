package com.studioedge.admin.global.support;

import com.studioedge.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Temporary compatibility adapter for legacy Admin REST controllers.
 * Remove each usage as its controller is converted to a Thymeleaf MVC flow.
 */
public final class HttpResponseUtil {

    private HttpResponseUtil() {
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(T data) {
        return ResponseEntity.ok(CommonResponse.ok(data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(data));
    }
}
