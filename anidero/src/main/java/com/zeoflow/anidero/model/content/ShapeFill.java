package com.zeoflow.anidero.model.content;

import android.graphics.Path;
import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.FillContent;
import com.zeoflow.anidero.model.animatable.AnimatableColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableIntegerValue;
import com.zeoflow.anidero.model.layer.BaseLayer;

public class ShapeFill implements ContentModel {
  private final boolean fillEnabled;
  private final Path.FillType fillType;
  private final String name;
  @Nullable private final AnimatableColorValue color;
  @Nullable private final AnimatableIntegerValue opacity;
  private final boolean hidden;

  public ShapeFill(String name, boolean fillEnabled, Path.FillType fillType,
                   @Nullable AnimatableColorValue color, @Nullable AnimatableIntegerValue opacity, boolean hidden) {
    this.name = name;
    this.fillEnabled = fillEnabled;
    this.fillType = fillType;
    this.color = color;
    this.opacity = opacity;
    this.hidden = hidden;
  }

  public String getName() {
    return name;
  }

  @Nullable public AnimatableColorValue getColor() {
    return color;
  }

  @Nullable public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public Path.FillType getFillType() {
    return fillType;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new FillContent(drawable, layer, this);
  }

  @Override
  public String toString() {
    return "ShapeFill{" + "color=" +
        ", fillEnabled=" + fillEnabled +
        '}';
  }
}
