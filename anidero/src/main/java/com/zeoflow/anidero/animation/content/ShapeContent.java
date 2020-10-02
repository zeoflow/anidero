package com.zeoflow.anidero.animation.content;

import android.graphics.Path;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.model.content.ShapePath;
import com.zeoflow.anidero.model.content.ShapeTrimPath;
import com.zeoflow.anidero.model.layer.BaseLayer;

import java.util.List;

public class ShapeContent implements PathContent, BaseKeyframeAnimation.AnimationListener {
  private final Path path = new Path();

  private final String name;
  private final boolean hidden;
  private final AnideroDrawable anideroDrawable;
  private final BaseKeyframeAnimation<?, Path> shapeAnimation;

  private boolean isPathValid;
  private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();

  public ShapeContent(AnideroDrawable anideroDrawable, BaseLayer layer, ShapePath shape) {
    name = shape.getName();
    hidden = shape.isHidden();
    this.anideroDrawable = anideroDrawable;
    shapeAnimation = shape.getShapePath().createAnimation();
    layer.addAnimation(shapeAnimation);
    shapeAnimation.addUpdateListener(this);
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
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
        // Trim path individually will be handled by the stroke where paths are combined.
        TrimPathContent trimPath = (TrimPathContent) content;
        trimPaths.addTrimPath(trimPath);
        trimPath.addListener(this);
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    if (hidden) {
      isPathValid = true;
      return path;
    }

    path.set(shapeAnimation.getValue());
    path.setFillType(Path.FillType.EVEN_ODD);

    trimPaths.apply(path);

    isPathValid = true;
    return path;
  }

  @Override public String getName() {
    return name;
  }
}
