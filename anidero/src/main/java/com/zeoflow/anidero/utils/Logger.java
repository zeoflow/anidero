package com.zeoflow.anidero.utils;

import com.zeoflow.anidero.AnideroLogger;

/**
 * Singleton object for logging. If you want to provide a custom logger implementation,
 * implements AnideroLogger interface in a custom class and replace Logger.instance
 */
public class Logger {

  private static AnideroLogger INSTANCE = new LogcatLogger();

  public static void setInstance(AnideroLogger instance) {
    Logger.INSTANCE = instance;
  }

  public static void debug(String message) {
    INSTANCE.debug(message);
  }

  public static void debug(String message, Throwable exception) {
    INSTANCE.debug(message, exception);
  }

  public static void warning(String message) {
    INSTANCE.warning(message);
  }

  public static void warning(String message, Throwable exception) {
    INSTANCE.warning(message, exception);
  }

  public static void error(String message, Throwable exception) {
    INSTANCE.error(message, exception);
  }
}
