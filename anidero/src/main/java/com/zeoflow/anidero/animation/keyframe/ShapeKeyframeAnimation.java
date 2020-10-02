package com.zeoflow.anidero.animation.keyframe;

import android.graphics.Path;

import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.model.content.ShapeData;
import com.zeoflow.anidero.utils.MiscUtils;

import java.util.List;

public class ShapeKeyframeAnimation extends BaseKeyframeAnimation<ShapeData, Path> {
  private final ShapeData tempShapeData = new ShapeData();
  private final Path tempPath = new Path();

  public ShapeKeyframeAnimation(List<Keyframe<ShapeData>> keyframes) {
    super(keyframes);
  }

  @Override public Path getValue(Keyframe<ShapeData> keyframe, float keyframeProgress) {
    ShapeData startShapeData = keyframe.startValue;
    ShapeData endShapeData = keyframe.endValue;

    tempShapeData.interpolateBetween(startShapeData, endShapeData, keyframeProgress);
    MiscUtils.getPathFromData(tempShapeData, tempPath);
    return tempPath;
  }
}
