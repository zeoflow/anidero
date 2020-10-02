package com.zeoflow.anidero.value;

import android.view.animation.Interpolator;

import com.zeoflow.anidero.utils.MiscUtils;

@SuppressWarnings("unused")
public class AnideroInterpolatedFloatValue extends AnideroInterpolatedValue<Float>
{

  public AnideroInterpolatedFloatValue(Float startValue, Float endValue) {
    super(startValue, endValue);
  }

  public AnideroInterpolatedFloatValue(Float startValue, Float endValue, Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override Float interpolateValue(Float startValue, Float endValue, float progress) {
    return MiscUtils.lerp(startValue, endValue, progress);
  }
}
