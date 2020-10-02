package com.zeoflow.anidero.value;

import android.graphics.PointF;
import androidx.annotation.NonNull;

import com.zeoflow.anidero.utils.MiscUtils;

/**
 * {@link AnideroValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AnideroRelativePointValueCallback extends AnideroValueCallback<PointF>
{
  private final PointF point = new PointF();

  public AnideroRelativePointValueCallback() {
  }

  public AnideroRelativePointValueCallback(@NonNull PointF staticValue) {
    super(staticValue);
  }

  @Override
  public final PointF getValue(AnideroFrameInfo<PointF> frameInfo) {
    point.set(
        MiscUtils.lerp(
            frameInfo.getStartValue().x,
            frameInfo.getEndValue().x,
            frameInfo.getInterpolatedKeyframeProgress()),
        MiscUtils.lerp(
            frameInfo.getStartValue().y,
            frameInfo.getEndValue().y,
            frameInfo.getInterpolatedKeyframeProgress())
    );

    PointF offset = getOffset(frameInfo);
    point.offset(offset.x, offset.y);
    return point;
  }

  /**
   * Override this to provide your own offset on every frame.
   */
  public PointF getOffset(AnideroFrameInfo<PointF> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
