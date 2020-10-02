package com.zeoflow.anidero.animation.keyframe;

import androidx.annotation.Nullable;
import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.value.AnideroFrameInfo;
import com.zeoflow.anidero.value.AnideroValueCallback;

import java.util.Collections;

public class ValueCallbackKeyframeAnimation<K, A> extends BaseKeyframeAnimation<K, A> {
  private final AnideroFrameInfo<A> frameInfo = new AnideroFrameInfo<>();

  private final A valueCallbackValue;

  public ValueCallbackKeyframeAnimation(AnideroValueCallback<A> valueCallback) {
    this(valueCallback, null);
  }

  public ValueCallbackKeyframeAnimation(AnideroValueCallback<A> valueCallback, @Nullable A valueCallbackValue) {
    super(Collections.<Keyframe<K>>emptyList());
    setValueCallback(valueCallback);
    this.valueCallbackValue = valueCallbackValue;
  }

  @Override public void setProgress(float progress) {
    this.progress = progress;
  }

  /**
   * If this doesn't return 1, then {@link #setProgress(float)} will always clamp the progress
   * to 0.
   */
  @Override float getEndProgress() {
    return 1f;
  }

  @Override public void notifyListeners() {
    if (this.valueCallback != null) {
      super.notifyListeners();
    }
  }

  @Override public A getValue() {
    //noinspection ConstantConditions
    return valueCallback.getValueInternal(0f, 0f, valueCallbackValue, valueCallbackValue, getProgress(), getProgress(), getProgress());
  }

  @Override A getValue(Keyframe<K> keyframe, float keyframeProgress) {
    return getValue();
  }
}
