package com.zeoflow.anidero.parser;


import com.zeoflow.anidero.parser.moshi.JsonReader;

import java.io.IOException;

public class FloatParser implements ValueParser<Float> {
  public static final FloatParser INSTANCE = new FloatParser();

  private FloatParser() {}

  @Override public Float parse(JsonReader reader, float scale) throws IOException {
    return JsonUtils.valueFromObject(reader) * scale;
  }
}