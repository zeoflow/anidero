package com.zeoflow.anidero.model.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.AnideroProperty;
import com.zeoflow.anidero.animation.LPaint;
import com.zeoflow.anidero.animation.keyframe.BaseKeyframeAnimation;
import com.zeoflow.anidero.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.zeoflow.anidero.utils.Utils;
import com.zeoflow.anidero.value.AnideroValueCallback;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

  ImageLayer(AnideroDrawable anideroDrawable, Layer layerModel) {
    super(anideroDrawable, layerModel);
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null || bitmap.isRecycled()) {
      return;
    }
    float density = Utils.dpScale();

    paint.setAlpha(parentAlpha);
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
    canvas.save();
    canvas.concat(parentMatrix);
    src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    dst.set(0, 0, (int) (bitmap.getWidth() * density), (int) (bitmap.getHeight() * density));
    canvas.drawBitmap(bitmap, src, dst , paint);
    canvas.restore();
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    Bitmap bitmap = getBitmap();
    if (bitmap != null) {
      outBounds.set(0, 0, bitmap.getWidth() * Utils.dpScale(), bitmap.getHeight() * Utils.dpScale());
      boundsMatrix.mapRect(outBounds);
    }
  }

  @Nullable
  private Bitmap getBitmap() {
    String refId = layerModel.getRefId();
    return anideroDrawable.getImageAsset(refId);
  }

  @SuppressWarnings("SingleStatementInBlock")
  @Override
  public <T> void addValueCallback(T property, @Nullable AnideroValueCallback<T> callback) {
    super.addValueCallback(property, callback);
     if (property == AnideroProperty.COLOR_FILTER) {
       if (callback == null) {
         colorFilterAnimation = null;
       } else {
         //noinspection unchecked
         colorFilterAnimation =
             new ValueCallbackKeyframeAnimation<>((AnideroValueCallback<ColorFilter>) callback);
       }
    }
  }
}
