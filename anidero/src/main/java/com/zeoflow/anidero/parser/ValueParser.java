package com.zeoflow.anidero.parser;


import com.zeoflow.anidero.parser.moshi.JsonReader;

import java.io.IOException;

interface ValueParser<V> {
  V parse(JsonReader reader, float scale) throws IOException;
}
