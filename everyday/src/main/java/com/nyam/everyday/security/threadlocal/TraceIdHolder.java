package com.nyam.everyday.security.threadlocal;

public class TraceIdHolder {

  private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

  public static void set(String traceId) {
    threadLocal.set(traceId);
  }
  public static String get() {
    return threadLocal.get();
  }
  public static void clear() {
    threadLocal.remove();
  }

}
