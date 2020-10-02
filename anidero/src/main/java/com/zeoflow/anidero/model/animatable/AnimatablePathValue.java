package com.zeoflow.anidero.model.animatable;

import android.graphics.PointF;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.PathKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.PointKeyframeAnimation;

import java.util.Collections;
import java.util.List;

public class AnimatablePathValue implements AnimatableValue<PointF, PointF> {
  private final List<Keyframe<PointF>> keyframes;

  /**
   * Create a default static animatable path.
   */
  public AnimatablePathValue() {
    keyframes = Collections.singletonList(new Keyframe<>(new PointF(0, 0)));
  }

  public AnimatablePathValue(List<Keyframe<PointF>> keyframes) {
    this.keyframes = keyframes;
  }

  @Override
  public List<Keyframe<PointF>> getKeyframes() {
    return keyframes;
  }

  @Override
  public boolean isStatic() {
    return keyframes.size() == 1 && keyframes.get(0).isStatic();
  }

  @Override
  public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
    if (keyframes.get(0).isStatic()) {
      return new PointKeyframeAnimation(keyframes);
    }
    return new PathKeyframeAnimation(keyframes);
  }
}
