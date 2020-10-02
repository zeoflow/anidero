package com.zeoflow.anidero.value;

import androidx.annotation.NonNull;

import com.zeoflow.anidero.utils.MiscUtils;

/**
 * {@link AnideroValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AnideroRelativeFloatValueCallback extends AnideroValueCallback<Float>
{

  public AnideroRelativeFloatValueCallback() {
  }

  public AnideroRelativeFloatValueCallback(@NonNull Float staticValue) {
    super(staticValue);
  }

  @Override
  public Float getValue(AnideroFrameInfo<Float> frameInfo) {
    float originalValue = MiscUtils.lerp(
        frameInfo.getStartValue(),
        frameInfo.getEndValue(),
        frameInfo.getInterpolatedKeyframeProgress()
    );
    float offset = getOffset(frameInfo);
    return originalValue + offset;
  }

  public Float getOffset(AnideroFrameInfo<Float> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
