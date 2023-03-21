package com.joejoe2.demo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.joejoe2.demo.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class IPUtilsTest {

  @Test
  void setRequestIP() {
    IPUtils.setRequestIP("127.0.0.1");
    assertEquals(
        "127.0.0.1",
        RequestContextHolder.currentRequestAttributes()
            .getAttribute("REQUEST_IP", RequestAttributes.SCOPE_REQUEST));
  }

  @Test
  void getRequestIP() {
    RequestContextHolder.currentRequestAttributes()
        .setAttribute("REQUEST_IP", "127.0.0.1", RequestAttributes.SCOPE_REQUEST);
    assertEquals("127.0.0.1", IPUtils.getRequestIP());
  }
}
