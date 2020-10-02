package com.zeoflow.anidero.animation.keyframe;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.model.DocumentData;

import java.util.List;

public class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
  public TextKeyframeAnimation(List<Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
    return keyframe.startValue;
  }
}
