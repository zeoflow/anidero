package com.zeoflow.anidero.value;

import android.view.animation.Interpolator;

import com.zeoflow.anidero.utils.MiscUtils;

@SuppressWarnings("unused")
public class AnideroInterpolatedIntegerValue extends AnideroInterpolatedValue<Integer>
{

  public AnideroInterpolatedIntegerValue(Integer startValue, Integer endValue) {
    super(startValue, endValue);
  }

  public AnideroInterpolatedIntegerValue(Integer startValue, Integer endValue, Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override Integer interpolateValue(Integer startValue, Integer endValue, float progress) {
    return MiscUtils.lerp(startValue, endValue, progress);
  }
}
