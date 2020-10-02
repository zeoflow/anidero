package com.zeoflow.anidero.model.content;

import com.zeoflow.anidero.AnideroDrawable;
import com.zeoflow.anidero.animation.content.Content;
import com.zeoflow.anidero.animation.content.ContentGroup;
import com.zeoflow.anidero.model.layer.BaseLayer;

import java.util.Arrays;
import java.util.List;

public class ShapeGroup implements ContentModel {
  private final String name;
  private final List<ContentModel> items;
  private final boolean hidden;

  public ShapeGroup(String name, List<ContentModel> items, boolean hidden) {
    this.name = name;
    this.items = items;
    this.hidden = hidden;
  }

  public String getName() {
    return name;
  }

  public List<ContentModel> getItems() {
    return items;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(AnideroDrawable drawable, BaseLayer layer) {
    return new ContentGroup(drawable, layer, this);
  }

  @Override public String toString() {
    return "ShapeGroup{" + "name='" + name + "\' Shapes: " + Arrays.toString(items.toArray()) + '}';
  }
}
