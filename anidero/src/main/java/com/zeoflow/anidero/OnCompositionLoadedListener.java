package com.zeoflow.anidero;

import androidx.annotation.Nullable;

/**
 * @see AnideroCompositionFactory
 * @see AnideroResult
 */
@Deprecated
public interface OnCompositionLoadedListener {
  /**
   * Composition will be null if there was an error loading it. Check logcat for more details.
   */
  void onCompositionLoaded(@Nullable AnideroComposition composition);
}
