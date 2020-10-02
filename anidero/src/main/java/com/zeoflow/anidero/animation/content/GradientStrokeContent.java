package com.zeoflow.anidero.animation.content;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.zeoflow.anidero.model.content.GradientColor;
import com.zeoflow.anidero.model.content.GradientStroke;
import com.zeoflow.anidero.model.content.GradientType;
import com.zeoflow.anidero.model.layer.BaseLayer;
import com.zeoflow.anidero.value.AnideroValueCallback;

public class GradientStrokeContent extends BaseStrokeContent {
  /**
   * Cache the gradients such that it runs at 30fps.
   */
  private static final int CACHE_STEPS_MS = 32;

  private final String name;
  private final boolean hidden;
  private final LongSparseArray<LinearGradient> linearGradientCache = new LongSparseArray<>();
  private final LongSparseArray<RadialGradient> radialGradientCache = new LongSparseArray<>();
  private final RectF boundsRect = new RectF();

  private final GradientType type;
  private final int cacheSteps;
  private final BaseKeyframeAnimation<GradientColor, GradientColor> colorAnimation;
  private final BaseKeyframeAnimation<PointF, PointF> startPointAnimation;
  private final BaseKeyframeAnimation<PointF, PointF> endPointAnimation;
  @Nullable private ValueCallbackKeyframeAnimation colorCallbackAnimation;

  public GradientStrokeContent(
      final AnideroDrawable anideroDrawable, BaseLayer layer, GradientStroke stroke) {
    super(anideroDrawable, layer, stroke.getCapType().toPaintCap(),
        stroke.getJoinType().toPaintJoin(), stroke.getMiterLimit(), stroke.getOpacity(),
        stroke.getWidth(), stroke.getLineDashPattern(), stroke.getDashOffset());

    name = stroke.getName();
    type = stroke.getGradientType();
    hidden = stroke.isHidden();
    cacheSteps = (int) (anideroDrawable.getComposition().getDuration() / CACHE_STEPS_MS);

    colorAnimation = stroke.getGradientColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);

    startPointAnimation = stroke.getStartPoint().createAnimation();
    startPointAnimation.addUpdateListener(this);
    layer.addAnimation(startPointAnimation);

    endPointAnimation = stroke.getEndPoint().createAnimation();
    endPointAnimation.addUpdateListener(this);
    layer.addAnimation(endPointAnimation);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    if (hidden) {
      return;
    }
    getBounds(boundsRect, parentMatrix, false);

    Shader shader;
    if (type == GradientType.LINEAR) {
      shader = getLinearGradient();
    } else {
      shader = getRadialGradient();
    }
    shader.setLocalMatrix(parentMatrix);
    paint.setShader(shader);

    super.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public String getName() {
    return name;
  }

  private LinearGradient getLinearGradient() {
    int gradientHash = getGradientHash();
    LinearGradient gradient = linearGradientCache.get(gradientHash);
    if (gradient != null) {
      return gradient;
    }
    PointF startPoint = startPointAnimation.getValue();
    PointF endPoint = endPointAnimation.getValue();
    GradientColor gradientColor = colorAnimation.getValue();
    int[] colors = applyDynamicColorsIfNeeded(gradientColor.getColors());
    float[] positions = gradientColor.getPositions();
    float x0 = startPoint.x;
    float y0 = startPoint.y;
    float x1 = endPoint.x;
    float y1 = endPoint.y;
    gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP);
    linearGradientCache.put(gradientHash, gradient);
    return gradient;
  }

  private RadialGradient getRadialGradient() {
    int gradientHash = getGradientHash();
    RadialGradient gradient = radialGradientCache.get(gradientHash);
    if (gradient != null) {
      return gradient;
    }
    PointF startPoint = startPointAnimation.getValue();
    PointF endPoint = endPointAnimation.getValue();
    GradientColor gradientColor = colorAnimation.getValue();
    int[] colors = applyDynamicColorsIfNeeded(gradientColor.getColors());
    float[] positions = gradientColor.getPositions();
    float x0 = startPoint.x;
    float y0 = startPoint.y;
    float x1 = endPoint.x;
    float y1 = endPoint.y;
    float r = (float) Math.hypot(x1 - x0, y1 - y0);
    gradient = new RadialGradient(x0, y0, r, colors, positions, Shader.TileMode.CLAMP);
    radialGradientCache.put(gradientHash, gradient);
    return gradient;
  }

  private int getGradientHash() {
    int startPointProgress = Math.round(startPointAnimation.getProgress() * cacheSteps);
    int endPointProgress = Math.round(endPointAnimation.getProgress() * cacheSteps);
    int colorProgress = Math.round(colorAnimation.getProgress() * cacheSteps);
    int hash = 17;
    if (startPointProgress != 0) {
      hash = hash * 31 * startPointProgress;
    }
    if (endPointProgress != 0) {
      hash = hash * 31 * endPointProgress;
    }
    if (colorProgress != 0) {
      hash = hash * 31 * colorProgress;
    }
    return hash;
  }

  private int[] applyDynamicColorsIfNeeded(int[] colors) {
    if (colorCallbackAnimation != null) {
      Integer[] dynamicColors = (Integer[]) colorCallbackAnimation.getValue();
      if (colors.length == dynamicColors.length) {
        for (int i = 0; i < colors.length; i++) {
          colors[i] = dynamicColors[i];
        }
      } else {
        colors = new int[dynamicColors.length];
        for (int i = 0; i < dynamicColors.length; i++) {
          colors[i] = dynamicColors[i];
        }
      }
    }
    return colors;
  }

  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == AnideroProperty.GRADIENT_COLOR) {
      if (colorCallbackAnimation != null) {
        layer.removeAnimation(colorCallbackAnimation);
      }

      if (callback == null) {
        colorCallbackAnimation = null;
      } else {
        colorCallbackAnimation = new ValueCallbackKeyframeAnimation<>(callback);
        colorCallbackAnimation.addUpdateListener(this);
        layer.addAnimation(colorCallbackAnimation);
      }
    }
  }
}
