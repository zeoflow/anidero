package com.zeoflow.anidero;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;

/**
 * Delegate to handle the loading of bitmaps that are not packaged in the assets of your app.
 *
 * @see AnideroDrawable#setImageAssetDelegate(ImageAssetDelegate)
 */
public interface ImageAssetDelegate {
  @Nullable Bitmap fetchBitmap(AnideroImageAsset asset);
}
