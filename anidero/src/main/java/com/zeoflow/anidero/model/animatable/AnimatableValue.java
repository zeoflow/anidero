package com.zeoflow.anidero.model.animatable;

import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.value.Keyframe;

import java.util.List;

public interface AnimatableValue<K, A> {
  List<Keyframe<K>> getKeyframes();
  boolean isStatic();
  BaseKeyframeAnimation<K, A> createAnimation();
}
