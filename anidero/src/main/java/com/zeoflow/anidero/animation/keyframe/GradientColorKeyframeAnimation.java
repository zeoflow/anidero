package com.zeoflow.anidero.animation.keyframe;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.model.content.GradientColor;

import java.util.List;

public class GradientColorKeyframeAnimation extends KeyframeAnimation<GradientColor> {
  private final GradientColor gradientColor;

  public GradientColorKeyframeAnimation(List<Keyframe<GradientColor>> keyframes) {
    super(keyframes);
    GradientColor startValue = keyframes.get(0).startValue;
    int size = startValue == null ? 0 : startValue.getSize();
    gradientColor = new GradientColor(new float[size], new int[size]);
  }

  @Override GradientColor getValue(Keyframe<GradientColor> keyframe, float keyframeProgress) {
    gradientColor.lerp(keyframe.startValue, keyframe.endValue, keyframeProgress);
    return gradientColor;
  }
}
