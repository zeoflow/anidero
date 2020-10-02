package com.zeoflow.anidero.model.animatable;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.animation.keyframe.TextKeyframeAnimation;
import com.zeoflow.anidero.model.DocumentData;

import java.util.List;

public class AnimatableTextFrame extends BaseAnimatableValue<DocumentData, DocumentData> {

  public AnimatableTextFrame(List<Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override public TextKeyframeAnimation createAnimation() {
    return new TextKeyframeAnimation(keyframes);
  }
}
