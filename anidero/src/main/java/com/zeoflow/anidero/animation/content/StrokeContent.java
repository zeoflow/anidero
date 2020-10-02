package com.zeoflow.anidero.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ColorKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.zeoflow.anidero.model.content.ShapeStroke;
import com.zeoflow.anidero.model.layer.BaseLayer;
import com.zeoflow.anidero.value.AnideroValueCallback;

import static com.zeoflow.anidero.AnideroProperty.STROKE_COLOR;

public class StrokeContent extends BaseStrokeContent {

  private final BaseLayer layer;
  private final String name;
  private final boolean hidden;
  private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

  public StrokeContent(final AnideroDrawable anideroDrawable, BaseLayer layer, ShapeStroke stroke) {
    super(anideroDrawable, layer, stroke.getCapType().toPaintCap(),
        stroke.getJoinType().toPaintJoin(), stroke.getMiterLimit(), stroke.getOpacity(),
        stroke.getWidth(), stroke.getLineDashPattern(), stroke.getDashOffset());
    this.layer = layer;
    name = stroke.getName();
    hidden = stroke.isHidden();
    colorAnimation = stroke.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    if (hidden) {
      return;
    }
    paint.setColor(((ColorKeyframeAnimation) colorAnimation).getIntValue());
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
    super.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == STROKE_COLOR) {
      colorAnimation.setValueCallback((AnideroValueCallback<Integer>) callback);
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
        layer.addAnimation(colorAnimation);
      }
    }
  }
}
