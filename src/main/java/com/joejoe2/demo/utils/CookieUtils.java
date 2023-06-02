package com.joejoe2.demo.utils;

import javax.servlet.http.Cookie;

public class CookieUtils {
  public static Cookie create(String key, String value, int maxAge, boolean isHttpOnly) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(maxAge);
    cookie.setHttpOnly(isHttpOnly);
    return cookie;
  }
}
