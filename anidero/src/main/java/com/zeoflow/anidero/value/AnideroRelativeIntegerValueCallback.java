package com.zeoflow.anidero.value;

import com.zeoflow.anidero.utils.MiscUtils;

/**
 * {@link AnideroValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AnideroRelativeIntegerValueCallback extends AnideroValueCallback<Integer>
{
  @Override
  public Integer getValue(AnideroFrameInfo<Integer> frameInfo) {
    int originalValue = MiscUtils.lerp(
        frameInfo.getStartValue(),
        frameInfo.getEndValue(),
        frameInfo.getInterpolatedKeyframeProgress()
    );
    int newValue = getOffset(frameInfo);
    return originalValue + newValue;
  }

  /**
   * Override this to provide your own offset on every frame.
   */
  public Integer getOffset(AnideroFrameInfo<Integer> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
