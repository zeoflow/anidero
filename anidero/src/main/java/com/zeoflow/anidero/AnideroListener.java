package com.zeoflow.anidero;

/**
 * Receive a result with either the value or exception for a {@link AnideroTask}
 */
public interface AnideroListener<T> {
  void onResult(T result);
}
