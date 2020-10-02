package com.zeoflow.anidero.model.content;

import android.graphics.PointF;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.EllipseContent;
import com.zeoflow.anidero.model.animatable.AnimatablePointValue;
import com.zeoflow.anidero.model.animatable.AnimatableValue;
import com.zeoflow.anidero.model.layer.BaseLayer;

public class CircleShape implements ContentModel {
  private final String name;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatablePointValue size;
  private final boolean isReversed;
  private final boolean hidden;

  public CircleShape(String name, AnimatableValue<PointF, PointF> position,
                     AnimatablePointValue size, boolean isReversed, boolean hidden) {
    this.name = name;
    this.position = position;
    this.size = size;
    this.isReversed = isReversed;
    this.hidden = hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new EllipseContent(drawable, layer, this);
  }

  public String getName() {
    return name;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }

  public boolean isReversed() {
    return isReversed;
  }

  public boolean isHidden() {
    return hidden;
  }
}
