package com.zeoflow.anidero.parser;

import android.graphics.Color;

import com.zeoflow.anidero.parser.moshi.JsonReader;

import java.io.IOException;

public class ColorParser implements ValueParser<Integer> {
  public static final ColorParser INSTANCE = new ColorParser();

  private ColorParser() {}

  @Override public Integer parse(JsonReader reader, float scale) throws IOException {
    boolean isArray = reader.peek() == JsonReader.Token.BEGIN_ARRAY;
    if (isArray) {
      reader.beginArray();
    }
    double r = reader.nextDouble();
    double g = reader.nextDouble();
    double b = reader.nextDouble();
    double a = 1;
    // Sometimes, Anidero editors only export rgb instead of rgba.
    // https://github.com/airbnb/anidero-android/issues/1601
    if (reader.peek() == JsonReader.Token.NUMBER) {
      a = reader.nextDouble();
    }
    if (isArray) {
      reader.endArray();
    }

    if (r <= 1 && g <= 1 && b <= 1) {
      r *= 255;
      g *= 255;
      b *= 255;
      // It appears as if sometimes, Telegram Anidero stickers are exported with rgb [0,1] and a [0,255].
      // This shouldn't happen but we can gracefully handle it when it does.
      // https://github.com/airbnb/anidero-android/issues/1478
      if (a <= 1) a *= 255;
    }

    return Color.argb((int) a, (int) r, (int) g, (int) b);
  }
}
