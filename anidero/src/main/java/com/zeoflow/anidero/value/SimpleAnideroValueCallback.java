package com.zeoflow.anidero.value;

/**
 * Delegate interface for {@link AnideroValueCallback}. This is helpful for the Kotlin API because you can use a SAM conversion to write the
 * callback as a single abstract method block like this:
 * animationView.addValueCallback(keyPath, AnideroProperty.TRANSFORM_OPACITY) { 50 }
 */
public interface SimpleAnideroValueCallback<T> {
  T getValue(AnideroFrameInfo<T> frameInfo);
}
