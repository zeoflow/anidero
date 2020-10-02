package com.zeoflow.anidero.parser;

import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroComposition;
import com.zeoflow.anidero.parser.moshi.JsonReader;
import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.model.animatable.AnimatableColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableFloatValue;
import com.zeoflow.anidero.model.animatable.AnimatableGradientColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableIntegerValue;
import com.zeoflow.anidero.model.animatable.AnimatablePointValue;
import com.zeoflow.anidero.model.animatable.AnimatableScaleValue;
import com.zeoflow.anidero.model.animatable.AnimatableShapeValue;
import com.zeoflow.anidero.model.animatable.AnimatableTextFrame;
import com.zeoflow.anidero.utils.Utils;

import java.io.IOException;
import java.util.List;

public class AnimatableValueParser {
  private AnimatableValueParser() {
  }

  public static AnimatableFloatValue parseFloat(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return parseFloat(reader, composition, true);
  }

  public static AnimatableFloatValue parseFloat(
      JsonReader reader, AnideroComposition composition, boolean isDp) throws IOException {
    return new AnimatableFloatValue(
        parse(reader, isDp ? Utils.dpScale() : 1f, composition, FloatParser.INSTANCE));
  }

  static AnimatableIntegerValue parseInteger(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatableIntegerValue(parse(reader, composition, IntegerParser.INSTANCE));
  }

  static AnimatablePointValue parsePoint(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatablePointValue(
        parse(reader, Utils.dpScale(), composition, PointFParser.INSTANCE));
  }

  static AnimatableScaleValue parseScale(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatableScaleValue(parse(reader, composition, ScaleXYParser.INSTANCE));
  }

  static AnimatableShapeValue parseShapeData(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatableShapeValue(
        parse(reader, Utils.dpScale(), composition, ShapeDataParser.INSTANCE));
  }

  static AnimatableTextFrame parseDocumentData(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatableTextFrame(parse(reader, composition, DocumentDataParser.INSTANCE));
  }

  static AnimatableColorValue parseColor(
      JsonReader reader, AnideroComposition composition) throws IOException {
    return new AnimatableColorValue(parse(reader, composition, ColorParser.INSTANCE));
  }

  static AnimatableGradientColorValue parseGradientColor(
      JsonReader reader, AnideroComposition composition, int points) throws IOException {
    return new AnimatableGradientColorValue(
        parse(reader, composition, new GradientColorParser(points)));
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  @Nullable private static <T> List<Keyframe<T>> parse(JsonReader reader,
                                                       AnideroComposition composition, ValueParser<T> valueParser) throws IOException {
    return KeyframesParser.parse(reader, composition, 1, valueParser);
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  @Nullable private static <T> List<Keyframe<T>> parse(JsonReader reader, float scale,
                                                       AnideroComposition composition, ValueParser<T> valueParser) throws IOException {
    return KeyframesParser.parse(reader, composition, scale, valueParser);
  }
}
