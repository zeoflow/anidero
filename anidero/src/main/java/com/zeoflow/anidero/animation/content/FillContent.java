package com.zeoflow.anidero.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.Nullable;

import com.zeoflow.anidero.Anidero;
import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.LPaint;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ColorKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.zeoflow.anidero.model.KeyPath;
import com.zeoflow.anidero.model.content.ShapeFill;
import com.zeoflow.anidero.model.layer.BaseLayer;
import com.zeoflow.anidero.utils.MiscUtils;
import com.zeoflow.anidero.value.AnideroValueCallback;

import java.util.ArrayList;
import java.util.List;

import static com.zeoflow.anidero.utils.MiscUtils.clamp;

public class FillContent
    implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private final Path path = new Path();
  private final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG);
  private final BaseLayer layer;
  private final String name;
  private final boolean hidden;
  private final List<PathContent> paths = new ArrayList<>();
  private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  private final BaseKeyframeAnimation<Integer, Integer> opacityAnimation;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  private final AnideroDrawable anideroDrawable;

  public FillContent(final AnideroDrawable anideroDrawable, BaseLayer layer, ShapeFill fill) {
    this.layer = layer;
    name = fill.getName();
    hidden = fill.isHidden();
    this.anideroDrawable = anideroDrawable;
    if (fill.getColor() == null || fill.getOpacity() == null ) {
      colorAnimation = null;
      opacityAnimation = null;
      return;
    }

    path.setFillType(fill.getFillType());

    colorAnimation = fill.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(this);
    layer.addAnimation(opacityAnimation);
  }

  @Override public void onValueChanged() {
    anideroDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    if (hidden) {
      return;
    }
    Anidero.beginSection("FillContent#draw");
    paint.setColor(((ColorKeyframeAnimation) colorAnimation).getIntValue());
    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(clamp(alpha, 0, 255));

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    canvas.drawPath(path, paint);

    Anidero.endSection("FillContent#draw");
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      this.path.addPath(paths.get(i).getPath(), parentMatrix);
    }
    path.computeBounds(outBounds, false);
    // Add padding to account for rounding errors.
    outBounds.set(
        outBounds.left - 1,
        outBounds.top - 1,
        outBounds.right + 1,
        outBounds.bottom + 1
    );
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    if (property == AnideroProperty.COLOR) {
      colorAnimation.setValueCallback((AnideroValueCallback<Integer>) callback);
    } else if (property == AnideroProperty.OPACITY) {
      opacityAnimation.setValueCallback((AnideroValueCallback<Integer>) callback);
    } else if (property == AnideroProperty.COLOR_FILTER) {
      if (colorFilterAnimation != null) {
        layer.removeAnimation(colorFilterAnimation);
      }

      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((AnideroValueCallback<ColorFilter>) callback);
        colorFilterAnimation.addUpdateListener(this);
        layer.addAnimation(colorFilterAnimation);
      }
    }
  }
}
