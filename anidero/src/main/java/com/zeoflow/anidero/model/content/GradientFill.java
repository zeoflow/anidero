package com.zeoflow.anidero.model.content;

import android.graphics.Path;
import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.GradientFillContent;
import com.zeoflow.anidero.model.animatable.AnimatableFloatValue;
import com.zeoflow.anidero.model.animatable.AnimatableGradientColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableIntegerValue;
import com.zeoflow.anidero.model.animatable.AnimatablePointValue;
import com.zeoflow.anidero.model.layer.BaseLayer;

public class GradientFill implements ContentModel {

  private final GradientType gradientType;
  private final Path.FillType fillType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final String name;
  @Nullable private final AnimatableFloatValue highlightLength;
  @Nullable private final AnimatableFloatValue highlightAngle;
  private final boolean hidden;

  public GradientFill(String name, GradientType gradientType, Path.FillType fillType,
                      AnimatableGradientColorValue gradientColor,
                      AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
                      AnimatablePointValue endPoint, AnimatableFloatValue highlightLength,
                      AnimatableFloatValue highlightAngle, boolean hidden) {
    this.gradientType = gradientType;
    this.fillType = fillType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.name = name;
    this.highlightLength = highlightLength;
    this.highlightAngle = highlightAngle;
    this.hidden = hidden;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
  }

  public Path.FillType getFillType() {
    return fillType;
  }

  public AnimatableGradientColorValue getGradientColor() {
    return gradientColor;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatablePointValue getStartPoint() {
    return startPoint;
  }

  public AnimatablePointValue getEndPoint() {
    return endPoint;
  }

  @Nullable AnimatableFloatValue getHighlightLength() {
    return highlightLength;
  }

  @Nullable AnimatableFloatValue getHighlightAngle() {
    return highlightAngle;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new GradientFillContent(drawable, layer, this);
  }

}
