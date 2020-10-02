package com.zeoflow.anidero.parser;

import android.graphics.Path;
import com.zeoflow.anidero.AnideroComposition;
import com.zeoflow.anidero.model.animatable.AnimatableColorValue;
import com.zeoflow.anidero.model.animatable.AnimatableIntegerValue;
import com.zeoflow.anidero.model.content.ShapeFill;
import com.zeoflow.anidero.parser.moshi.JsonReader;
import com.zeoflow.anidero.value.Keyframe;

import java.io.IOException;
import java.util.Collections;

class ShapeFillParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "c",
      "o",
      "fillEnabled",
      "r",
      "hd"
  );

  private ShapeFillParser() {
  }

  static ShapeFill parse(
      JsonReader reader, AnideroComposition composition) throws IOException {
    AnimatableColorValue color = null;
    boolean fillEnabled = false;
    AnimatableIntegerValue opacity = null;
    String name = null;
    int fillTypeInt = 1;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          color = AnimatableValueParser.parseColor(reader, composition);
          break;
        case 2:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 3:
          fillEnabled = reader.nextBoolean();
          break;
        case 4:
          fillTypeInt = reader.nextInt();
          break;
        case 5:
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    // Telegram sometimes omits opacity.
    // https://github.com/airbnb/anidero-android/issues/1600
    opacity = opacity == null ? new AnimatableIntegerValue(Collections.singletonList(new Keyframe<>(100))) : opacity;
    Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
    return new ShapeFill(name, fillEnabled, fillType, color, opacity, hidden);
  }
}
