package com.joejoe2.demo.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void randomNumericCode() {
    assertThrows(IllegalArgumentException.class, () -> Utils.randomNumericCode(-1));
    assertThrows(IllegalArgumentException.class, () -> Utils.randomNumericCode(0));

    Pattern pattern = Pattern.compile("[0-9]+");
    for (int i = 1; i <= 10; i++) {
      String code = Utils.randomNumericCode(i);
      assertEquals(i, code.length());
      assertTrue(pattern.matcher(code).matches());
    }
  }
}
