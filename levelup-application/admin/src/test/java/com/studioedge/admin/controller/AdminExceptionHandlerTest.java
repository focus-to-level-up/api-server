package com.studioedge.admin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.assertj.core.api.Assertions.assertThat;

class AdminExceptionHandlerTest {

    private final AdminExceptionHandler handler = new AdminExceptionHandler();

    @Test
    void rendersCommonErrorPageForUnexpectedException() {
        ConcurrentModel model = new ConcurrentModel();

        String view = handler.handleUnexpectedException(new RuntimeException("internal detail"), model);

        assertThat(view).isEqualTo("error/500");
        assertThat(model.getAttribute("message")).isEqualTo("요청을 처리하는 중 문제가 발생했습니다.");
    }
}
