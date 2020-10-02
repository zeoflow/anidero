package com.zeoflow.anidero.parser;

import android.graphics.PointF;

import com.zeoflow.anidero.AnideroComposition;
import com.zeoflow.anidero.parser.moshi.JsonReader;
import com.zeoflow.anidero.value.Keyframe;
import com.zeoflow.anidero.animation.keyframe.PathKeyframe;
import com.zeoflow.anidero.utils.Utils;

import java.io.IOException;

class PathKeyframeParser {

  private PathKeyframeParser() {}

  static PathKeyframe parse(
      JsonReader reader, AnideroComposition composition) throws IOException {
    boolean animated = reader.peek() == JsonReader.Token.BEGIN_OBJECT;
    Keyframe<PointF> keyframe = KeyframeParser.parse(
        reader, composition, Utils.dpScale(), PathParser.INSTANCE, animated);

    return new PathKeyframe(composition, keyframe);
  }
}
