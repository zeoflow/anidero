package com.zeoflow.anidero.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.zeoflow.anidero.AnideroDrawable;

public class NullLayer extends BaseLayer {
  NullLayer(AnideroDrawable anideroDrawable, Layer layerModel) {
    super(anideroDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    // Do nothing.
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    outBounds.set(0, 0, 0, 0);
  }
}
