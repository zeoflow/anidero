package com.zeoflow.anidero.model.content;


import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.model.layer.BaseLayer;

public interface ContentModel {
  @Nullable Content toContent(AnideroDrawable drawable, BaseLayer layer);
}
