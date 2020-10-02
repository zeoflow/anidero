package com.zeoflow.anidero.model.animatable;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.GradientColorKeyframeAnimation;
import com.zeoflow.anidero.model.content.GradientColor;

import java.util.List;

public class AnimatableGradientColorValue extends BaseAnimatableValue<GradientColor,
    GradientColor> {
  public AnimatableGradientColorValue(
      List<Keyframe<GradientColor>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<GradientColor, GradientColor> createAnimation() {
    return new GradientColorKeyframeAnimation(keyframes);
  }
}
