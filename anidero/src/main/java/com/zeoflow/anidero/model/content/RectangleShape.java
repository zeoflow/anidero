package com.zeoflow.anidero.model.content;

import android.graphics.PointF;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.RectangleContent;
import com.zeoflow.anidero.model.animatable.AnimatableFloatValue;
import com.zeoflow.anidero.model.animatable.AnimatablePointValue;
import com.zeoflow.anidero.model.animatable.AnimatableValue;
import com.zeoflow.anidero.model.layer.BaseLayer;

public class RectangleShape implements ContentModel {
  private final String name;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatablePointValue size;
  private final AnimatableFloatValue cornerRadius;
  private final boolean hidden;

  public RectangleShape(String name, AnimatableValue<PointF, PointF> position,
                        AnimatablePointValue size, AnimatableFloatValue cornerRadius, boolean hidden) {
    this.name = name;
    this.position = position;
    this.size = size;
    this.cornerRadius = cornerRadius;
    this.hidden = hidden;
  }

  public String getName() {
    return name;
  }

  public AnimatableFloatValue getCornerRadius() {
    return cornerRadius;
  }

  public AnimatablePointValue getSize() {
    return size;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new RectangleContent(drawable, layer, this);
  }

  @Override public String toString() {
    return "RectangleShape{position=" + position +
        ", size=" + size +
        '}';
  }
}
