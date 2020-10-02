package com.zeoflow.anidero.network;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.zeoflow.anidero.AnideroComposition;
import com.zeoflow.anidero.AnideroCompositionFactory;
import com.zeoflow.anidero.AnideroResult;
import com.zeoflow.anidero.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipInputStream;

public class NetworkFetcher {

  private final Context appContext;
  private final String url;

  @Nullable private final NetworkCache networkCache;

  public static AnideroResult<AnideroComposition> fetchSync(Context context, String url, @Nullable String cacheKey) {
    return new NetworkFetcher(context, url, cacheKey).fetchSync();
  }

  private NetworkFetcher(Context context, String url, @Nullable String cacheKey) {
    appContext = context.getApplicationContext();
    this.url = url;
    if (cacheKey == null) {
      networkCache = null;
    } else {
      networkCache = new NetworkCache(appContext);
    }
  }

  @WorkerThread
  public AnideroResult<AnideroComposition> fetchSync() {
    AnideroComposition result = fetchFromCache();
    if (result != null) {
      return new AnideroResult<>(result);
    }

    Logger.debug("Animation for " + url + " not found in cache. Fetching from network.");
    return fetchFromNetwork();
  }

  /**
   * Returns null if the animation doesn't exist in the cache.
   */
  @Nullable
  @WorkerThread
  private AnideroComposition fetchFromCache() {
    if (networkCache == null) {
      return null;
    }
    Pair<FileExtension, InputStream> cacheResult = networkCache.fetch(url);
    if (cacheResult == null) {
      return null;
    }

    FileExtension extension = cacheResult.first;
    InputStream inputStream = cacheResult.second;
    AnideroResult<AnideroComposition> result;
    if (extension == FileExtension.ZIP) {
      result = AnideroCompositionFactory.fromZipStreamSync(new ZipInputStream(inputStream), url);
    } else {
      result = AnideroCompositionFactory.fromJsonInputStreamSync(inputStream, url);
    }
    if (result.getValue() != null) {
      return result.getValue();
    }
    return null;
  }

  @WorkerThread
  private AnideroResult<AnideroComposition> fetchFromNetwork() {
    try {
      return fetchFromNetworkInternal();
    } catch (IOException e) {
      return new AnideroResult<>(e);
    }
  }

  @WorkerThread
  private AnideroResult<AnideroComposition> fetchFromNetworkInternal() throws IOException {
    Logger.debug("Fetching " + url);


    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");

    try {
      connection.connect();

      if (connection.getErrorStream() != null || connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        String error = getErrorFromConnection(connection);
        return new AnideroResult<AnideroComposition>(new IllegalArgumentException("Unable to fetch " + url + ". Failed with " + connection.getResponseCode() + "\n" + error));
      }

      AnideroResult<AnideroComposition> result = getResultFromConnection(connection);
      Logger.debug("Completed fetch from network. Success: " + (result.getValue() != null));
      return result;
    } catch (Exception e) {
      return new AnideroResult<>(e);
    } finally {
      connection.disconnect();
    }
  }

  private String getErrorFromConnection(HttpURLConnection connection) throws IOException {
    int responseCode = connection.getResponseCode();
    BufferedReader r = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
    StringBuilder error = new StringBuilder();
    String line;

    try {
      while ((line = r.readLine()) != null) {
        error.append(line).append('\n');
      }
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        r.close();
      } catch (Exception e) {
        // Do nothing.
      }
    }
    return error.toString();
  }

  @Nullable
  private AnideroResult<AnideroComposition> getResultFromConnection(HttpURLConnection connection) throws IOException {
    File file;
    FileExtension extension;
    AnideroResult<AnideroComposition> result;
    String contentType = connection.getContentType();
    if (contentType == null) {
      // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
      // in the result which is more useful than failing here.
      contentType = "application/json";
    }
    if (contentType.contains("application/zip")) {
      Logger.debug("Handling zip response.");
      extension = FileExtension.ZIP;
      if (networkCache == null) {
        result = AnideroCompositionFactory.fromZipStreamSync(new ZipInputStream(connection.getInputStream()), null);
      } else {
        file = networkCache.writeTempCacheFile(url, connection.getInputStream(), extension);
        result = AnideroCompositionFactory.fromZipStreamSync(new ZipInputStream(new FileInputStream(file)), url);
      }
    } else {
      Logger.debug("Received json response.");
      extension = FileExtension.JSON;
      if (networkCache == null) {
        result = AnideroCompositionFactory.fromJsonInputStreamSync(connection.getInputStream(), null);
      } else {
        file = networkCache.writeTempCacheFile(url, connection.getInputStream(), extension);
        result = AnideroCompositionFactory.fromJsonInputStreamSync(new FileInputStream(new File(file.getAbsolutePath())), url);
      }
    }

    if (networkCache != null && result.getValue() != null) {
      networkCache.renameTempFile(url, extension);
    }
    return result;
  }
}
