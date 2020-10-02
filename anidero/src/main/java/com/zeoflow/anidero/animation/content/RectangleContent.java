package com.zeoflow.anidero.animation.content;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.FloatKeyframeAnimation;
import com.zeoflow.anidero.model.KeyPath;
import com.zeoflow.anidero.model.content.RectangleShape;
import com.zeoflow.anidero.model.content.ShapeTrimPath;
import com.zeoflow.anidero.model.layer.BaseLayer;
import com.zeoflow.anidero.utils.MiscUtils;
import com.zeoflow.anidero.value.AnideroValueCallback;

import java.util.List;

import androidx.annotation.Nullable;

public class RectangleContent
    implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, PathContent {
  private final Path path = new Path();
  private final RectF rect = new RectF();

  private final String name;
  private final boolean hidden;
  private final AnideroDrawable anideroDrawable;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;
  private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
  private final BaseKeyframeAnimation<?, Float> cornerRadiusAnimation;

  private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();
  private boolean isPathValid;

  public RectangleContent(AnideroDrawable anideroDrawable, BaseLayer layer, RectangleShape rectShape) {
    name = rectShape.getName();
    hidden = rectShape.isHidden();
    this.anideroDrawable = anideroDrawable;
    positionAnimation = rectShape.getPosition().createAnimation();
    sizeAnimation = rectShape.getSize().createAnimation();
    cornerRadiusAnimation = rectShape.getCornerRadius().createAnimation();

    layer.addAnimation(positionAnimation);
    layer.addAnimation(sizeAnimation);
    layer.addAnimation(cornerRadiusAnimation);

    positionAnimation.addUpdateListener(this);
    sizeAnimation.addUpdateListener(this);
    cornerRadiusAnimation.addUpdateListener(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void onValueChanged() {
    invalidate();
  }

  private void invalidate() {
    isPathValid = false;
    anideroDrawable.invalidateSelf();
  }

  @Override
  public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
        TrimPathContent trimPath = (TrimPathContent) content;
        trimPaths.addTrimPath(trimPath);
        trimPath.addListener(this);
      }
    }
  }

  @Override
  public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    if (hidden) {
      isPathValid = true;
      return path;
    }

    PointF size = sizeAnimation.getValue();
    float halfWidth = size.x / 2f;
    float halfHeight = size.y / 2f;
    float radius = cornerRadiusAnimation == null ?
        0f : ((FloatKeyframeAnimation) cornerRadiusAnimation).getFloatValue();
    float maxRadius = Math.min(halfWidth, halfHeight);
    if (radius > maxRadius) {
      radius = maxRadius;
    }

    // Draw the rectangle top right to bottom left.
    PointF position = positionAnimation.getValue();

    path.moveTo(position.x + halfWidth, position.y - halfHeight + radius);

    path.lineTo(position.x + halfWidth, position.y + halfHeight - radius);

    if (radius > 0) {
      rect.set(position.x + halfWidth - 2 * radius,
          position.y + halfHeight - 2 * radius,
          position.x + halfWidth,
          position.y + halfHeight);
      path.arcTo(rect, 0, 90, false);
    }

    path.lineTo(position.x - halfWidth + radius, position.y + halfHeight);

    if (radius > 0) {
      rect.set(position.x - halfWidth,
          position.y + halfHeight - 2 * radius,
          position.x - halfWidth + 2 * radius,
          position.y + halfHeight);
      path.arcTo(rect, 90, 90, false);
    }

    path.lineTo(position.x - halfWidth, position.y - halfHeight + radius);

    if (radius > 0) {
      rect.set(position.x - halfWidth,
          position.y - halfHeight,
          position.x - halfWidth + 2 * radius,
          position.y - halfHeight + 2 * radius);
      path.arcTo(rect, 180, 90, false);
    }

    path.lineTo(position.x + halfWidth - radius, position.y - halfHeight);

    if (radius > 0) {
      rect.set(position.x + halfWidth - 2 * radius,
          position.y - halfHeight,
          position.x + halfWidth,
          position.y - halfHeight + 2 * radius);
      path.arcTo(rect, 270, 90, false);
    }
    path.close();

    trimPaths.apply(path);

    isPathValid = true;
    return path;
  }

  @Override
  public void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
                             KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    if (property == AnideroProperty.RECTANGLE_SIZE) {
      sizeAnimation.setValueCallback((AnideroValueCallback<PointF>) callback);
    } else if (property == AnideroProperty.POSITION) {
      positionAnimation.setValueCallback((AnideroValueCallback<PointF>) callback);
    } else if (property == AnideroProperty.CORNER_RADIUS) {
      cornerRadiusAnimation.setValueCallback((AnideroValueCallback<Float>) callback);
    }
  }
}
