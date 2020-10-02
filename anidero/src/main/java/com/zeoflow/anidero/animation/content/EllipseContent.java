package com.zeoflow.anidero.animation.content;

import android.graphics.Path;
import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.model.KeyPath;
import com.zeoflow.anidero.model.content.CircleShape;
import com.zeoflow.anidero.model.content.ShapeTrimPath;
import com.zeoflow.anidero.model.layer.BaseLayer;
import com.zeoflow.anidero.utils.MiscUtils;
import com.zeoflow.anidero.value.AnideroValueCallback;

import java.util.List;

public class EllipseContent
    implements PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

  private final Path path = new Path();

  private final String name;
  private final AnideroDrawable anideroDrawable;
  private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;
  private final CircleShape circleShape;

  private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();
  private boolean isPathValid;

  public EllipseContent(AnideroDrawable anideroDrawable, BaseLayer layer, CircleShape circleShape) {
    name = circleShape.getName();
    this.anideroDrawable = anideroDrawable;
    sizeAnimation = circleShape.getSize().createAnimation();
    positionAnimation = circleShape.getPosition().createAnimation();
    this.circleShape = circleShape;

    layer.addAnimation(sizeAnimation);
    layer.addAnimation(positionAnimation);

    sizeAnimation.addUpdateListener(this);
    positionAnimation.addUpdateListener(this);
  }

  @Override public void onValueChanged() {
    invalidate();
  }

  private void invalidate() {
    isPathValid = false;
    anideroDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent && ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
        TrimPathContent trimPath = (TrimPathContent) content;
        trimPaths.addTrimPath(trimPath);
        trimPath.addListener(this);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    if (circleShape.isHidden()) {
      isPathValid = true;
      return path;
    }

    PointF size = sizeAnimation.getValue();
    float halfWidth = size.x / 2f;
    float halfHeight = size.y / 2f;
    // TODO: handle bounds

    float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
    float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

    path.reset();
    if (circleShape.isReversed()) {
      path.moveTo(0, -halfHeight);
      path.cubicTo(0 - cpW, -halfHeight, -halfWidth, 0 - cpH, -halfWidth, 0);
      path.cubicTo(-halfWidth, 0 + cpH, 0 - cpW, halfHeight, 0, halfHeight);
      path.cubicTo(0 + cpW, halfHeight, halfWidth, 0 + cpH, halfWidth, 0);
      path.cubicTo(halfWidth, 0 - cpH, 0 + cpW, -halfHeight, 0, -halfHeight);
    } else {
      path.moveTo(0, -halfHeight);
      path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0);
      path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0, halfHeight);
      path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0);
      path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0, -halfHeight);
    }

    PointF position = positionAnimation.getValue();
    path.offset(position.x, position.y);

    path.close();

    trimPaths.apply(path);

    isPathValid = true;
    return path;
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    if (property == AnideroProperty.ELLIPSE_SIZE) {
      sizeAnimation.setValueCallback((AnideroValueCallback<PointF>) callback);
    } else if (property == AnideroProperty.POSITION) {
      positionAnimation.setValueCallback((AnideroValueCallback<PointF>) callback);
    }
  }
}
