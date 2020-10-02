package com.zeoflow.anidero;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zeoflow.anidero.model.AnideroCompositionCache;
import com.zeoflow.anidero.network.NetworkCache;
import com.zeoflow.anidero.network.NetworkFetcher;
import com.zeoflow.anidero.parser.AnideroCompositionMoshiParser;
import com.zeoflow.anidero.parser.moshi.JsonReader;

import com.zeoflow.anidero.utils.Utils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.WorkerThread;

import static com.zeoflow.anidero.parser.moshi.JsonReader.*;
import static com.zeoflow.anidero.utils.Utils.closeQuietly;
import static okio.Okio.buffer;
import static okio.Okio.source;

/**
 * Helpers to create or cache a AnideroComposition.
 * <p>
 * All factory methods take a cache key. The animation will be stored in an LRU cache for future use.
 * In-progress tasks will also be held so they can be returned for subsequent requests for the same
 * animation prior to the cache being populated.
 */
@SuppressWarnings({"WeakerAccess", "unused", "NullAway"})
public class AnideroCompositionFactory
{
  /**
   * Keep a map of cache keys to in-progress tasks and return them for new requests.
   * Without this, simultaneous requests to parse a composition will trigger multiple parallel
   * parse tasks prior to the cache getting populated.
   */
  private static final Map<String, AnideroTask<AnideroComposition>> taskCache = new HashMap<>();

  private AnideroCompositionFactory() {
  }

  /**
   * Set the maximum number of compositions to keep cached in memory.
   * This must be > 0.
   */
  public static void setMaxCacheSize(int size) {
    AnideroCompositionCache.getInstance().resize(size);
  }

  public static void clearCache(Context context) {
    taskCache.clear();
    AnideroCompositionCache.getInstance().clear();
    new NetworkCache(context).clear();
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Anidero will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   *
   * To skip the cache, add null as a third parameter.
   */
  public static AnideroTask<AnideroComposition> fromUrl(final Context context, final String url) {
    return fromUrl(context, url, "url_" + url);
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Anidero will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  public static AnideroTask<AnideroComposition> fromUrl(final Context context, final String url, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return NetworkFetcher.fetchSync(context, url, cacheKey);
      }
    });
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Anidero will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromUrlSync(Context context, String url) {
    return fromUrlSync(context, url, url);
  }


  /**
   * Fetch an animation from an http url. Once it is downloaded once, Anidero will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromUrlSync(Context context, String url, @Nullable String cacheKey) {
    return NetworkFetcher.fetchSync(context, url, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * To skip the cache, add null as a third parameter.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static AnideroTask<AnideroComposition> fromAsset(Context context, final String fileName) {
    String cacheKey = "asset_" + fileName;
    return fromAsset(context, fileName, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static AnideroTask<AnideroComposition> fromAsset(Context context, final String fileName, @Nullable final String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return fromAssetSync(appContext, fileName, cacheKey);
      }
    });
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * To skip the cache, add null as a third parameter.
   *
   * @see #fromZipStreamSync(ZipInputStream, String)
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromAssetSync(Context context, String fileName) {
      String cacheKey = "asset_" + fileName;
      return fromAssetSync(context, fileName, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStreamSync(ZipInputStream, String)
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromAssetSync(Context context, String fileName, @Nullable String cacheKey) {
    try {
      if (fileName.endsWith(".zip")) {
        return fromZipStreamSync(new ZipInputStream(context.getAssets().open(fileName)), cacheKey);
      }
      return fromJsonInputStreamSync(context.getAssets().open(fileName), cacheKey);
    } catch (IOException e) {
      return new AnideroResult<>(e);
    }
  }


  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * To skip the cache, add null as a third parameter.
   */
  public static AnideroTask<AnideroComposition> fromRawRes(Context context, @RawRes final int rawRes) {
    return fromRawRes(context, rawRes, rawResCacheKey(context, rawRes));
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * Pass null as the cache key to skip caching.
   */
  public static AnideroTask<AnideroComposition> fromRawRes(Context context, @RawRes final int rawRes, @Nullable final String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final WeakReference<Context> contextRef = new WeakReference<>(context);
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        @Nullable Context originalContext = contextRef.get();
        Context context = originalContext != null ? originalContext : appContext;
        return fromRawResSync(context, rawRes, cacheKey);
      }
    });
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * To skip the cache, add null as a third parameter.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromRawResSync(Context context, @RawRes int rawRes) {
    return fromRawResSync(context, rawRes, rawResCacheKey(context, rawRes));
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * Pass null as the cache key to skip caching.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromRawResSync(Context context, @RawRes int rawRes, @Nullable String cacheKey) {
    try {
      return fromJsonInputStreamSync(context.getResources().openRawResource(rawRes), cacheKey);
    } catch (Resources.NotFoundException e) {
      return new AnideroResult<>(e);
    }
  }

  private static String rawResCacheKey(Context context, @RawRes int resId) {
    return "rawRes" + (isNightMode(context) ? "_night_" : "_day_") + resId;
  }

  /**
   * It is important to include day/night in the cache key so that if it changes, the cache won't return an animation from the wrong bucket.
   */
  private static boolean isNightMode(Context context) {
    int nightModeMasked = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeMasked == Configuration.UI_MODE_NIGHT_YES;
  }

  /**
   * Auto-closes the stream.
   *
   * @see #fromJsonInputStreamSync(InputStream, String, boolean)
   */
  public static AnideroTask<AnideroComposition> fromJsonInputStream(final InputStream stream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return fromJsonInputStreamSync(stream, cacheKey);
      }
    });
  }

  /**
   * Return a AnideroComposition for the given InputStream to json.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromJsonInputStreamSync(InputStream stream, @Nullable String cacheKey) {
    return fromJsonInputStreamSync(stream, cacheKey, true);
  }


  @WorkerThread
  private static AnideroResult<AnideroComposition> fromJsonInputStreamSync(InputStream stream, @Nullable String cacheKey, boolean close) {
    try {
      return fromJsonReaderSync(of(buffer(source(stream))), cacheKey);
    } finally {
      if (close) {
        closeQuietly(stream);
      }
    }
  }


  /**
   * @see #fromJsonSync(JSONObject, String)
   */
  @Deprecated
  public static AnideroTask<AnideroComposition> fromJson(final JSONObject json, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        //noinspection deprecation
        return fromJsonSync(json, cacheKey);
      }
    });
  }

  /**
   * Prefer passing in the json string directly. This method just calls `toString()` on your JSONObject.
   * If you are loading this animation from the network, just use the response body string instead of
   * parsing it first for improved performance.
   */
  @Deprecated
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromJsonSync(JSONObject json, @Nullable String cacheKey) {
    return fromJsonStringSync(json.toString(), cacheKey);
  }

  /**
   * @see #fromJsonStringSync(String, String)
   */
  public static AnideroTask<AnideroComposition> fromJsonString(final String json, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return fromJsonStringSync(json, cacheKey);
      }
    });
  }

  /**
   * Return a AnideroComposition for the specified raw json string.
   * If loading from a file, it is preferable to use the InputStream or rawRes version.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromJsonStringSync(String json, @Nullable String cacheKey) {


    ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
    return fromJsonReaderSync(of(buffer(source(stream))), cacheKey);
  }

  public static AnideroTask<AnideroComposition> fromJsonReader(final JsonReader reader, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return fromJsonReaderSync(reader, cacheKey);
      }
    });
  }


  @WorkerThread
  public static AnideroResult<AnideroComposition> fromJsonReaderSync(com.zeoflow.anidero.parser.moshi.JsonReader reader, @Nullable String cacheKey) {
    return fromJsonReaderSyncInternal(reader, cacheKey, true);
  }


  private static AnideroResult<AnideroComposition> fromJsonReaderSyncInternal(
      com.zeoflow.anidero.parser.moshi.JsonReader reader, @Nullable String cacheKey, boolean close) {
    try {
      AnideroComposition composition = AnideroCompositionMoshiParser.parse(reader);
      if (cacheKey != null) {
        AnideroCompositionCache.getInstance().put(cacheKey, composition);
      }
      return new AnideroResult<>(composition);
    } catch (Exception e) {
      return new AnideroResult<>(e);
    } finally {
      if (close) {
        closeQuietly(reader);
      }
    }
  }


  public static AnideroTask<AnideroComposition> fromZipStream(final ZipInputStream inputStream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<AnideroResult<AnideroComposition>>() {
      @Override
      public AnideroResult<AnideroComposition> call() {
        return fromZipStreamSync(inputStream, cacheKey);
      }
    });
  }

  /**
   * Parses a zip input stream into a Anidero composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   */
  @WorkerThread
  public static AnideroResult<AnideroComposition> fromZipStreamSync(ZipInputStream inputStream, @Nullable String cacheKey) {
    try {
      return fromZipStreamSyncInternal(inputStream, cacheKey);
    } finally {
      closeQuietly(inputStream);
    }
  }

  @WorkerThread
  private static AnideroResult<AnideroComposition> fromZipStreamSyncInternal(ZipInputStream inputStream, @Nullable String cacheKey) {
    AnideroComposition composition = null;
    Map<String, Bitmap> images = new HashMap<>();

    try {
      ZipEntry entry = inputStream.getNextEntry();
      while (entry != null) {
        final String entryName = entry.getName();
        if (entryName.contains("__MACOSX")) {
          inputStream.closeEntry();
        } else if (entry.getName().contains(".json")) {
          com.zeoflow.anidero.parser.moshi.JsonReader reader = of(buffer(source(inputStream)));
          composition = AnideroCompositionFactory.fromJsonReaderSyncInternal(reader, null, false).getValue();
        } else if (entryName.contains(".png") || entryName.contains(".webp")) {
          String[] splitName = entryName.split("/");
          String name = splitName[splitName.length - 1];
          images.put(name, BitmapFactory.decodeStream(inputStream));
        } else {
          inputStream.closeEntry();
        }

        entry = inputStream.getNextEntry();
      }
    } catch (IOException e) {
      return new AnideroResult<>(e);
    }


    if (composition == null) {
      return new AnideroResult<>(new IllegalArgumentException("Unable to parse composition"));
    }

    for (Map.Entry<String, Bitmap> e : images.entrySet()) {
      AnideroImageAsset imageAsset = findImageAssetForFileName(composition, e.getKey());
      if (imageAsset != null) {
        imageAsset.setBitmap(Utils.resizeBitmapIfNeeded(e.getValue(), imageAsset.getWidth(), imageAsset.getHeight()));
      }
    }

    // Ensure that all bitmaps have been set.
    for (Map.Entry<String, AnideroImageAsset> entry : composition.getImages().entrySet()) {
      if (entry.getValue().getBitmap() == null) {
        return new AnideroResult<>(new IllegalStateException("There is no image for " + entry.getValue().getFileName()));
      }
    }

    if (cacheKey != null) {
      AnideroCompositionCache.getInstance().put(cacheKey, composition);
    }
    return new AnideroResult<>(composition);
  }

  @Nullable
  private static AnideroImageAsset findImageAssetForFileName(AnideroComposition composition, String fileName) {
    for (AnideroImageAsset asset : composition.getImages().values()) {
      if (asset.getFileName().equals(fileName)) {
        return asset;
      }
    }
    return null;
  }

  /**
   * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
   * If not, create a new task for the callable.
   * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
   */
  private static AnideroTask<AnideroComposition> cache(
      @Nullable final String cacheKey, Callable<AnideroResult<AnideroComposition>> callable) {
    final AnideroComposition cachedComposition = cacheKey == null ? null : AnideroCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new AnideroTask<>(new Callable<AnideroResult<AnideroComposition>>() {
        @Override
        public AnideroResult<AnideroComposition> call() {
          return new AnideroResult<>(cachedComposition);
        }
      });
    }
    if (cacheKey != null && taskCache.containsKey(cacheKey)) {
      return taskCache.get(cacheKey);
    }

    AnideroTask<AnideroComposition> task = new AnideroTask<>(callable);
    if (cacheKey != null) {
      task.addListener(new AnideroListener<AnideroComposition>() {
        @Override
        public void onResult(AnideroComposition result) {
          taskCache.remove(cacheKey);
        }
      });
      task.addFailureListener(new AnideroListener<Throwable>() {
        @Override
        public void onResult(Throwable result) {
          taskCache.remove(cacheKey);
        }
      });
      taskCache.put(cacheKey, task);
    }
    return task;
  }
}
