package com.joejoe2.demo.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IPUtilsTest {

    @Test
    void setRequestIP() {
        IPUtils.setRequestIP("127.0.0.1");
        assertEquals("127.0.0.1", RequestContextHolder.currentRequestAttributes().getAttribute(
                "REQUEST_IP",
                RequestAttributes.SCOPE_REQUEST));
    }

    @Test
    void getRequestIP() {
        RequestContextHolder.currentRequestAttributes().setAttribute(
                "REQUEST_IP",
                "127.0.0.1",
                RequestAttributes.SCOPE_REQUEST);
        assertEquals("127.0.0.1", IPUtils.getRequestIP());
    }
}