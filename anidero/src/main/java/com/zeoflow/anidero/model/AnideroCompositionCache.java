package com.zeoflow.anidero.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.LruCache;

import com.zeoflow.anidero.AnideroComposition;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnideroCompositionCache
{

  private static final AnideroCompositionCache INSTANCE = new AnideroCompositionCache();

  public static AnideroCompositionCache getInstance() {
    return INSTANCE;
  }

  private final LruCache<String, AnideroComposition> cache = new LruCache<>(20);

  @VisibleForTesting
  AnideroCompositionCache() {
  }

  @Nullable
  public AnideroComposition get(@Nullable String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return cache.get(cacheKey);
  }

  public void put(@Nullable String cacheKey, AnideroComposition composition) {
    if (cacheKey == null) {
      return;
    }
    cache.put(cacheKey, composition);
  }

  public void clear() {
    cache.evictAll();
  }

  /**
   * Set the maximum number of compositions to keep cached in memory.
   * This must be > 0.
   */
  public void resize(int size) {
    cache.resize(size);
  }
}
