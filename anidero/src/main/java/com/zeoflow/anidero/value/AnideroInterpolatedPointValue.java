package com.zeoflow.anidero.value;

import android.graphics.PointF;
import android.view.animation.Interpolator;

import com.zeoflow.anidero.utils.MiscUtils;

@SuppressWarnings("unused")
public class AnideroInterpolatedPointValue extends AnideroInterpolatedValue<PointF>
{
  private final PointF point = new PointF();

  public AnideroInterpolatedPointValue(PointF startValue, PointF endValue) {
    super(startValue, endValue);
  }

  public AnideroInterpolatedPointValue(PointF startValue, PointF endValue, Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override PointF interpolateValue(PointF startValue, PointF endValue, float progress) {
    point.set(
        MiscUtils.lerp(startValue.x, endValue.x, progress),
        MiscUtils.lerp(startValue.y, endValue.y, progress)
    );
    return point;
  }
}
