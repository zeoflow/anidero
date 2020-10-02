package com.zeoflow.anidero.animation.keyframe;

import com.zeoflow.anidero.value.Keyframe;

import java.util.List;

abstract class KeyframeAnimation<T> extends BaseKeyframeAnimation<T, T> {
  KeyframeAnimation(List<? extends Keyframe<T>> keyframes) {
    super(keyframes);
  }
}
