package com.zeoflow.anidero.value;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

abstract class AnideroInterpolatedValue<T> extends AnideroValueCallback<T>
{

  private final T startValue;
  private final T endValue;
  private final Interpolator interpolator;

  AnideroInterpolatedValue(T startValue, T endValue) {
    this(startValue, endValue, new LinearInterpolator());
  }

  AnideroInterpolatedValue(T startValue, T endValue, Interpolator interpolator) {
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
  }

  @Override public T getValue(AnideroFrameInfo<T> frameInfo) {
    float progress = interpolator.getInterpolation(frameInfo.getOverallProgress());
    return interpolateValue(this.startValue, this.endValue, progress);
  }

  abstract T interpolateValue(T startValue, T endValue, float progress);
}
