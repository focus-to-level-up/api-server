package com.studioedge.focus_to_levelup_server.domain;

import io.sentry.Sentry;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
public class TestController {
    @GetMapping("/test")
    public void test() {
        try {
            throw new Exception("This is a test.");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
