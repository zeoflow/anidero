package com.zeoflow.anidero.parser;

import android.graphics.PointF;

import com.zeoflow.anidero.AnideroComposition;
import com.zeoflow.anidero.parser.moshi.JsonReader;
import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.model.animatable.AnimatableFloatValue;
import com.zeoflow.anidero.model.animatable.AnimatablePathValue;
import com.zeoflow.anidero.model.animatable.AnimatableSplitDimensionPathValue;
import com.zeoflow.anidero.model.animatable.AnimatableValue;
import com.zeoflow.anidero.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimatablePathValueParser {

  private static JsonReader.Options NAMES = JsonReader.Options.of(
      "k",
      "x",
      "y"
  );

  private AnimatablePathValueParser() {}

  public static AnimatablePathValue parse(
      JsonReader reader, AnideroComposition composition) throws IOException {
    List<Keyframe<PointF>> keyframes = new ArrayList<>();
    if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
      reader.beginArray();
      while (reader.hasNext()) {
        keyframes.add(PathKeyframeParser.parse(reader, composition));
      }
      reader.endArray();
      KeyframesParser.setEndFrames(keyframes);
    } else {
      keyframes.add(new Keyframe<>(JsonUtils.jsonToPoint(reader, Utils.dpScale())));
    }
    return new AnimatablePathValue(keyframes);
  }

  /**
   * Returns either an {@link AnimatablePathValue} or an {@link AnimatableSplitDimensionPathValue}.
   */
  static AnimatableValue<PointF, PointF> parseSplitPath(
      JsonReader reader, AnideroComposition composition) throws IOException {

    AnimatablePathValue pathAnimation = null;
    AnimatableFloatValue xAnimation = null;
    AnimatableFloatValue yAnimation = null;

    boolean hasExpressions = false;

    reader.beginObject();
    while (reader.peek() != JsonReader.Token.END_OBJECT) {
      switch (reader.selectName(NAMES)) {
        case 0:
          pathAnimation = AnimatablePathValueParser.parse(reader, composition);
          break;
        case 1:
          if (reader.peek() == JsonReader.Token.STRING) {
            hasExpressions = true;
            reader.skipValue();
          } else {
            xAnimation = AnimatableValueParser.parseFloat(reader, composition);
          }
          break;
        case 2:
          if (reader.peek() == JsonReader.Token.STRING) {
            hasExpressions = true;
            reader.skipValue();
          } else {
            yAnimation = AnimatableValueParser.parseFloat(reader, composition);
          }
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    if (hasExpressions) {
      composition.addWarning("Anidero doesn't support expressions.");
    }

    if (pathAnimation != null) {
      return pathAnimation;
    }
    return new AnimatableSplitDimensionPathValue(xAnimation, yAnimation);
  }

}
