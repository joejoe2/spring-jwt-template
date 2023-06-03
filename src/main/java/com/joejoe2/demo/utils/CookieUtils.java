package com.joejoe2.demo.utils;

import javax.servlet.http.Cookie;

public class CookieUtils {
  public static Cookie create(
      String key, String value, String domain, int maxAge, boolean isHttpOnly) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(maxAge);
    cookie.setDomain(domain);
    cookie.setPath("/");
    cookie.setHttpOnly(isHttpOnly);
    return cookie;
  }

  public static Cookie removed(String key, String domain, boolean isHttpOnly) {
    Cookie cookie = new Cookie(key, null);
    cookie.setMaxAge(0);
    cookie.setDomain(domain);
    cookie.setPath("/");
    cookie.setHttpOnly(isHttpOnly);
    return cookie;
  }
}
