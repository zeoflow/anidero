package com.zeoflow.anidero.model.content;

import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.GradientStrokeContent;
import com.zeoflow.anidero.model.animatable.AnimatableFloatValue;
import com.zeoflow.anidero.model.animatable.AnimatableGradientColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableIntegerValue;
import com.zeoflow.anidero.model.animatable.AnimatablePointValue;
import com.zeoflow.anidero.model.layer.BaseLayer;

import java.util.List;

public class GradientStroke implements ContentModel {

  private final String name;
  private final GradientType gradientType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final AnimatableFloatValue width;
  private final ShapeStroke.LineCapType capType;
  private final ShapeStroke.LineJoinType joinType;
  private final float miterLimit;
  private final List<AnimatableFloatValue> lineDashPattern;
  @Nullable private final AnimatableFloatValue dashOffset;
  private final boolean hidden;

  public GradientStroke(String name, GradientType gradientType,
                        AnimatableGradientColorValue gradientColor,
                        AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
                        AnimatablePointValue endPoint, AnimatableFloatValue width, ShapeStroke.LineCapType capType,
                        ShapeStroke.LineJoinType joinType, float miterLimit,
                        List<AnimatableFloatValue> lineDashPattern,
                        @Nullable AnimatableFloatValue dashOffset, boolean hidden) {
    this.name = name;
    this.gradientType = gradientType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
    this.miterLimit = miterLimit;
    this.lineDashPattern = lineDashPattern;
    this.dashOffset = dashOffset;
    this.hidden = hidden;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
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

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public ShapeStroke.LineCapType getCapType() {
    return capType;
  }

  public ShapeStroke.LineJoinType getJoinType() {
    return joinType;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  @Nullable public AnimatableFloatValue getDashOffset() {
    return dashOffset;
  }

  public float getMiterLimit() {
    return miterLimit;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new GradientStrokeContent(drawable, layer, this);
  }
}
