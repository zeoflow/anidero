package com.zeoflow.anidero;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.view.ViewCompat;

import com.zeoflow.anidero.model.KeyPath;
import com.zeoflow.anidero.parser.moshi.JsonReader;
import com.zeoflow.anidero.utils.Logger;
import com.zeoflow.anidero.utils.Utils;
import com.zeoflow.anidero.value.AnideroFrameInfo;
import com.zeoflow.anidero.value.AnideroValueCallback;
import com.zeoflow.anidero.value.SimpleAnideroValueCallback;
import com.zeoflow.material.elements.imageview.MaterialImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.zeoflow.anidero.RenderMode.HARDWARE;

/**
 * This view will load, deserialize, and display an After Effects animation exported with
 * bodymovin (https://github.com/bodymovin/bodymovin).
 * <p>
 * You may set the animation in one of two ways:
 * 1) Attrs: {@link R.styleable#AnideroAnimationView_anidero_fileName}
 * 2) Programmatically:
 *      {@link #setAnimation(String)}
 *      {@link #setAnimation(JsonReader, String)}
 *      {@link #setAnimationFromJson(String, String)}
 *      {@link #setAnimationFromUrl(String)}
 *      {@link #setComposition(AnideroComposition)}
 * <p>
 * You can set a default cache strategy with {@link R.attr#anidero_cacheStrategy}.
 * <p>
 * You can manually set the progress of the animation with {@link #setProgress(float)} or
 * {@link R.attr#anidero_progress}
 *
 * @see <a href="http://airbnb.io/anidero">Full Documentation</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public class AnideroAnimationView extends MaterialImageView
{

  private static final String TAG = AnideroAnimationView.class.getSimpleName();
  private static final AnideroListener<Throwable> DEFAULT_FAILURE_LISTENER = new AnideroListener<Throwable>() {
    @Override public void onResult(Throwable throwable) {
      // By default, fail silently for network errors.
      if (Utils.isNetworkException(throwable)) {
        Logger.warning("Unable to load composition.", throwable);
        return;
      }
      throw new IllegalStateException("Unable to parse composition", throwable);
    }
  };

  private final AnideroListener<AnideroComposition> loadedListener = new AnideroListener<AnideroComposition>() {
    @Override public void onResult(AnideroComposition composition) {
      setComposition(composition);
    }
  };

  private final AnideroListener<Throwable> wrappedFailureListener = new AnideroListener<Throwable>() {
    @Override
    public void onResult(Throwable result) {
      if (fallbackResource != 0) {
        setImageResource(fallbackResource);
      }
      AnideroListener<Throwable> l = failureListener == null ? DEFAULT_FAILURE_LISTENER : failureListener;
      l.onResult(result);
    }
  };
  @Nullable private AnideroListener<Throwable> failureListener;
  @DrawableRes private int fallbackResource = 0;

  private final AnideroDrawable anideroDrawable = new AnideroDrawable();
  private boolean isInitialized;
  private String animationName;
  private @RawRes int animationResId;

  private boolean playAnimationWhenShown = false;
  private boolean wasAnimatingWhenNotShown = false;
  private boolean wasAnimatingWhenDetached = false;

  private boolean autoPlay = false;
  private boolean cacheComposition = true;
  private RenderMode renderMode = RenderMode.AUTOMATIC;
  private Set<AnideroOnCompositionLoadedListener> anideroOnCompositionLoadedListeners = new HashSet<>();
  /**
   * Prevents a StackOverflowException on 4.4 in which getDrawingCache() calls buildDrawingCache().
   * This isn't a great solution but it works and has very little performance overhead.
   * At some point in the future, the original goal of falling back to hardware rendering when
   * the animation is set to software rendering but it is too large to fit in a software bitmap
   * should be reevaluated.
   */
  private int buildDrawingCacheDepth = 0;

  @Nullable private AnideroTask<AnideroComposition> compositionTask;
  /** Can be null because it is created async */
  @Nullable private AnideroComposition composition;

  public AnideroAnimationView(Context context) {
    super(context);
    init(null, R.attr.anideroAnimationViewStyle);
  }

  public AnideroAnimationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, R.attr.anideroAnimationViewStyle);
  }

  public AnideroAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.AnideroAnimationView, defStyleAttr, 0);
    cacheComposition = ta.getBoolean(R.styleable.AnideroAnimationView_anidero_cacheComposition, true);
    boolean hasRawRes = ta.hasValue(R.styleable.AnideroAnimationView_anidero_rawRes);
    boolean hasFileName = ta.hasValue(R.styleable.AnideroAnimationView_anidero_fileName);
    boolean hasUrl = ta.hasValue(R.styleable.AnideroAnimationView_anidero_url);
    if (hasRawRes && hasFileName) {
      throw new IllegalArgumentException("anidero_rawRes and anidero_fileName cannot be used at " +
          "the same time. Please use only one at once.");
    } else if (hasRawRes) {
      int rawResId = ta.getResourceId(R.styleable.AnideroAnimationView_anidero_rawRes, 0);
      if (rawResId != 0) {
        setAnimation(rawResId);
      }
    } else if (hasFileName) {
      String fileName = ta.getString(R.styleable.AnideroAnimationView_anidero_fileName);
      if (fileName != null) {
        setAnimation(fileName);
      }
    } else if (hasUrl) {
      String url = ta.getString(R.styleable.AnideroAnimationView_anidero_url);
      if (url != null) {
        setAnimationFromUrl(url);
      }
    }

    setFallbackResource(ta.getResourceId(R.styleable.AnideroAnimationView_anidero_fallbackRes, 0));
    if (ta.getBoolean(R.styleable.AnideroAnimationView_anidero_autoPlay, false)) {
      wasAnimatingWhenDetached = true;
      autoPlay = true;
    }

    if (ta.getBoolean(R.styleable.AnideroAnimationView_anidero_loop, false)) {
      anideroDrawable.setRepeatCount(AnideroDrawable.INFINITE);
    }

    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_repeatMode)) {
      setRepeatMode(ta.getInt(R.styleable.AnideroAnimationView_anidero_repeatMode,
          AnideroDrawable.RESTART));
    }

    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_repeatCount)) {
      setRepeatCount(ta.getInt(R.styleable.AnideroAnimationView_anidero_repeatCount,
          AnideroDrawable.INFINITE));
    }

    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_speed)) {
      setSpeed(ta.getFloat(R.styleable.AnideroAnimationView_anidero_speed, 1f));
    }

    setImageAssetsFolder(ta.getString(R.styleable.AnideroAnimationView_anidero_imageAssetsFolder));
    setProgress(ta.getFloat(R.styleable.AnideroAnimationView_anidero_progress, 0));
    enableMergePathsForKitKatAndAbove(ta.getBoolean(
        R.styleable.AnideroAnimationView_anidero_enableMergePathsForKitKatAndAbove, false));
    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_colorFilter)) {
      SimpleColorFilter filter = new SimpleColorFilter(
          ta.getColor(R.styleable.AnideroAnimationView_anidero_colorFilter, Color.TRANSPARENT));
      KeyPath keyPath = new KeyPath("**");
      AnideroValueCallback<ColorFilter> callback = new AnideroValueCallback<ColorFilter>(filter);
      addValueCallback(keyPath, AnideroProperty.COLOR_FILTER, callback);
    }
    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_scale)) {
      anideroDrawable.setScale(ta.getFloat(R.styleable.AnideroAnimationView_anidero_scale, 1f));
    }

    if (ta.hasValue(R.styleable.AnideroAnimationView_anidero_renderMode)) {
      int renderModeOrdinal = ta.getInt(R.styleable.AnideroAnimationView_anidero_renderMode, RenderMode.AUTOMATIC.ordinal());
      if (renderModeOrdinal >= RenderMode.values().length) {
        renderModeOrdinal = RenderMode.AUTOMATIC.ordinal();
      }
      setRenderMode(RenderMode.values()[renderModeOrdinal]);
    }

    if (getScaleType() != null) {
      anideroDrawable.setScaleType(getScaleType());
    }
    ta.recycle();

    anideroDrawable.setSystemAnimationsAreEnabled(Utils.getAnimationScale(getContext()) != 0f);

    enableOrDisableHardwareLayer();
    isInitialized = true;
  }

  @Override public void setImageResource(int resId) {
    cancelLoaderTask();
    super.setImageResource(resId);
  }

  @Override public void setImageDrawable(Drawable drawable) {
    cancelLoaderTask();
    super.setImageDrawable(drawable);
  }

  @Override public void setImageBitmap(Bitmap bm) {
    cancelLoaderTask();
    super.setImageBitmap(bm);
  }

  @Override public void invalidateDrawable(@NonNull Drawable dr) {
    if (getDrawable() == anideroDrawable) {
      // We always want to invalidate the root drawable so it redraws the whole drawable.
      // Eventually it would be great to be able to invalidate just the changed region.
      super.invalidateDrawable(anideroDrawable);
    } else {
      // Otherwise work as regular ImageView
      super.invalidateDrawable(dr);
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.animationName = animationName;
    ss.animationResId = animationResId;
    ss.progress = anideroDrawable.getProgress();
    ss.isAnimating = anideroDrawable.isAnimating() || (!ViewCompat.isAttachedToWindow(this) && wasAnimatingWhenDetached);
    ss.imageAssetsFolder = anideroDrawable.getImageAssetsFolder();
    ss.repeatMode = anideroDrawable.getRepeatMode();
    ss.repeatCount = anideroDrawable.getRepeatCount();
    return ss;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    animationName = ss.animationName;
    if (!TextUtils.isEmpty(animationName)) {
      setAnimation(animationName);
    }
    animationResId = ss.animationResId;
    if (animationResId != 0) {
      setAnimation(animationResId);
    }
    setProgress(ss.progress);
    if (ss.isAnimating) {
      playAnimation();
    }
    anideroDrawable.setImagesAssetsFolder(ss.imageAssetsFolder);
    setRepeatMode(ss.repeatMode);
    setRepeatCount(ss.repeatCount);
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    // This can happen on older versions of Android because onVisibilityChanged gets called from the
    // constructor of View so this will get called before anideroDrawable gets initialized.
    // https://github.com/airbnb/anidero-android/issues/1143
    // A simple null check on anideroDrawable would not work because when using Proguard optimization, a
    // null check on a final field gets removed. As "usually" final fields cannot be null.
    // However because this is called by super (View) before the initializer of the AnideroAnimationView
    // is called, it actually can be null here.
    // Working around this by using a non final boolean that is set to true after the class initializer
    // has run.
    if (!isInitialized) {
      return;
    }
    if (isShown()) {
      if (wasAnimatingWhenNotShown) {
        resumeAnimation();
      } else if (playAnimationWhenShown) {
        playAnimation();
      }
      wasAnimatingWhenNotShown = false;
      playAnimationWhenShown = false;
    } else {
      if (isAnimating()) {
        pauseAnimation();
        wasAnimatingWhenNotShown = true;
      }
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!isInEditMode() &&( autoPlay || wasAnimatingWhenDetached)) {
      playAnimation();
      // Autoplay from xml should only apply once.
      autoPlay = false;
      wasAnimatingWhenDetached = false;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      // This is needed to mimic newer platform behavior.
      // https://stackoverflow.com/a/53625860/715633
      onVisibilityChanged(this, getVisibility());
    }
  }

  @Override protected void onDetachedFromWindow() {
    if (isAnimating()) {
      cancelAnimation();
      wasAnimatingWhenDetached = true;
    }
    super.onDetachedFromWindow();
  }

  /**
   * Enable this to get merge path support for devices running KitKat (19) and above.
   *
   * Merge paths currently don't work if the the operand shape is entirely contained within the
   * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
   * instead of using merge paths.
   */
  public void enableMergePathsForKitKatAndAbove(boolean enable) {
    anideroDrawable.enableMergePathsForKitKatAndAbove(enable);
  }

  /**
   * Returns whether merge paths are enabled for KitKat and above.
   */
  public boolean isMergePathsEnabledForKitKatAndAbove() {
    return anideroDrawable.isMergePathsEnabledForKitKatAndAbove();
  }

  /**
   * If set to true, all future compositions that are set will be cached so that they don't need to be parsed
   * next time they are loaded. This won't apply to compositions that have already been loaded.
   *
   * Defaults to true.
   *
   * {@link R.attr#anidero_cacheComposition}
   */
  public void  setCacheComposition(boolean cacheComposition) {
    this.cacheComposition = cacheComposition;
  }

  /**
   * Sets the animation from a file in the raw directory.
   * This will load and deserialize the file asynchronously.
   */
  public void setAnimation(@RawRes final int rawRes) {
    this.animationResId = rawRes;
    animationName = null;
    setCompositionTask(fromRawRes(rawRes));
  }


  private AnideroTask<AnideroComposition> fromRawRes(@RawRes final int rawRes) {
    if (isInEditMode()) {
      return new AnideroTask<>(new Callable<AnideroResult<AnideroComposition>>() {
        @Override public AnideroResult<AnideroComposition> call() throws Exception {
          return cacheComposition
              ? AnideroCompositionFactory.fromRawResSync(getContext(), rawRes) : AnideroCompositionFactory.fromRawResSync(getContext(), rawRes, null);
        }
      }, true);
    } else {
      return cacheComposition ?
          AnideroCompositionFactory.fromRawRes(getContext(), rawRes) : AnideroCompositionFactory.fromRawRes(getContext(), rawRes, null);
    }
  }

  public void setAnimation(final String assetName) {
    this.animationName = assetName;
    animationResId = 0;
    setCompositionTask(fromAssets(assetName));
  }

  private AnideroTask<AnideroComposition> fromAssets(final String assetName) {
    if (isInEditMode()) {
      return new AnideroTask<>(new Callable<AnideroResult<AnideroComposition>>() {
        @Override public AnideroResult<AnideroComposition> call() throws Exception {
          return cacheComposition ?
              AnideroCompositionFactory.fromAssetSync(getContext(), assetName) : AnideroCompositionFactory.fromAssetSync(getContext(), assetName, null);
        }
      }, true);
    } else {
      return cacheComposition ?
          AnideroCompositionFactory.fromAsset(getContext(), assetName) : AnideroCompositionFactory.fromAsset(getContext(), assetName, null);
    }
  }

  /**
   * @see #setAnimationFromJson(String, String)
   */
  @Deprecated
  public void setAnimationFromJson(String jsonString) {
    setAnimationFromJson(jsonString, null);
  }

  /**
   * Sets the animation from json string. This is the ideal API to use when loading an animation
   * over the network because you can use the raw response body here and a conversion to a
   * JSONObject never has to be done.
   */
  public void setAnimationFromJson(String jsonString, @Nullable String cacheKey) {
    setAnimation(new ByteArrayInputStream(jsonString.getBytes()), cacheKey);
  }

  /**
   * Sets the animation from an arbitrary InputStream.
   * This will load and deserialize the file asynchronously.
   * <p>
   * This is particularly useful for animations loaded from the network. You can fetch the
   * bodymovin json from the network and pass it directly here.
   */
  public void setAnimation(InputStream stream, @Nullable String cacheKey) {
    setCompositionTask(AnideroCompositionFactory.fromJsonInputStream(stream, cacheKey));
  }

  /**
   * Load a anidero animation from a url. The url can be a json file or a zip file. Use a zip file if you have images. Simply zip them together and anidero
   * will unzip and link the images automatically.
   *
   * Under the hood, Anidero uses Java HttpURLConnection because it doesn't require any transitive networking dependencies. It will download the file
   * to the application cache under a temporary name. If the file successfully parses to a composition, it will rename the temporary file to one that
   * can be accessed immediately for subsequent requests. If the file does not parse to a composition, the temporary file will be deleted.
   */
  public void setAnimationFromUrl(String url) {
    AnideroTask<AnideroComposition> task = cacheComposition ?
        AnideroCompositionFactory.fromUrl(getContext(), url) : AnideroCompositionFactory.fromUrl(getContext(), url, null);
    setCompositionTask(task);
  }

  /**
   * Load a anidero animation from a url. The url can be a json file or a zip file. Use a zip file if you have images. Simply zip them together and anidero
   * will unzip and link the images automatically.
   *
   * Under the hood, Anidero uses Java HttpURLConnection because it doesn't require any transitive networking dependencies. It will download the file
   * to the application cache under a temporary name. If the file successfully parses to a composition, it will rename the temporary file to one that
   * can be accessed immediately for subsequent requests. If the file does not parse to a composition, the temporary file will be deleted.
   */
  public void setAnimationFromUrl(String url, @Nullable String cacheKey) {
    AnideroTask<AnideroComposition> task = AnideroCompositionFactory.fromUrl(getContext(), url, cacheKey);
    setCompositionTask(task);
  }

  /**
   * Set a default failure listener that will be called if any of the setAnimation APIs fail for any reason.
   * This can be used to replace the default behavior.
   *
   * The default behavior will log any network errors and rethrow all other exceptions.
   *
   * If you are loading an animation from the network, errors may occur if your user has no internet.
   * You can use this listener to retry the download or you can have it default to an error drawable
   * with {@link #setFallbackResource(int)}.
   *
   * Unless you are using {@link #setAnimationFromUrl(String)}, errors are unexpected.
   *
   * Set the listener to null to revert to the default behavior.
   */
  public void setFailureListener(@Nullable AnideroListener<Throwable> failureListener) {
    this.failureListener = failureListener;
  }

  /**
   * Set a drawable that will be rendered if the AnideroComposition fails to load for any reason.
   * Unless you are using {@link #setAnimationFromUrl(String)}, this is an unexpected error and
   * you should handle it with {@link #setFailureListener(AnideroListener)}.
   *
   * If this is a network animation, you may use this to show an error to the user or
   * you can use a failure listener to retry the download.
   */
  public void setFallbackResource(@DrawableRes int fallbackResource) {
    this.fallbackResource = fallbackResource;
  }

  private void setCompositionTask(AnideroTask<AnideroComposition> compositionTask) {
    clearComposition();
    cancelLoaderTask();
    this.compositionTask = compositionTask
            .addListener(loadedListener)
            .addFailureListener(wrappedFailureListener);
  }

  private void cancelLoaderTask() {
    if (compositionTask != null) {
      compositionTask.removeListener(loadedListener);
      compositionTask.removeFailureListener(wrappedFailureListener);
    }
  }

  /**
   * Sets a composition.
   * You can set a default cache strategy if this view was inflated with xml by
   * using {@link R.attr#anidero_cacheStrategy}.
   */
  public void setComposition(@NonNull AnideroComposition composition) {
    if (Anidero.DBG) {
      Log.v(TAG, "Set Composition \n" + composition);
    }
    anideroDrawable.setCallback(this);

    this.composition = composition;
    boolean isNewComposition = anideroDrawable.setComposition(composition);
    enableOrDisableHardwareLayer();
    if (getDrawable() == anideroDrawable && !isNewComposition) {
      // We can avoid re-setting the drawable, and invalidating the view, since the composition
      // hasn't changed.
      return;
    }

    // This is needed to makes sure that the animation is properly played/paused for the current visibility state.
    // It is possible that the drawable had a lazy composition task to play the animation but this view subsequently
    // became invisible. Comment this out and run the espresso tests to see a failing test.
    onVisibilityChanged(this, getVisibility());

    requestLayout();

    for (AnideroOnCompositionLoadedListener anideroOnCompositionLoadedListener : anideroOnCompositionLoadedListeners) {
        anideroOnCompositionLoadedListener.onCompositionLoaded(composition);
    }

  }

  @Nullable public AnideroComposition getComposition() {
    return composition;
  }

  /**
   * Returns whether or not any layers in this composition has masks.
   */
  public boolean hasMasks() {
    return anideroDrawable.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  public boolean hasMatte() {
    return anideroDrawable.hasMatte();
  }

  /**
   * Plays the animation from the beginning. If speed is < 0, it will start at the end
   * and play towards the beginning
   */
  @MainThread
  public void playAnimation() {
    if (isShown()) {
      anideroDrawable.playAnimation();
      enableOrDisableHardwareLayer();
    } else {
      playAnimationWhenShown = true;
    }
  }

  /**
   * Continues playing the animation from its current position. If speed < 0, it will play backwards
   * from the current position.
   */
  @MainThread
  public void resumeAnimation() {
    if (isShown()) {
      anideroDrawable.resumeAnimation();
      enableOrDisableHardwareLayer();
    } else {
      playAnimationWhenShown = false;
      wasAnimatingWhenNotShown = true;
    }
  }

  /**
   * Sets the minimum frame that the animation will start from when playing or looping.
   */
  public void setMinFrame(int startFrame) {
    anideroDrawable.setMinFrame(startFrame);
  }

  /**
   * Returns the minimum frame set by {@link #setMinFrame(int)} or {@link #setMinProgress(float)}
   */
  public float getMinFrame() {
    return anideroDrawable.getMinFrame();
  }

  /**
   * Sets the minimum progress that the animation will start from when playing or looping.
   */
  public void setMinProgress(float startProgress) {
    anideroDrawable.setMinProgress(startProgress);
  }

  /**
   * Sets the maximum frame that the animation will end at when playing or looping.
   */
  public void setMaxFrame(int endFrame) {
    anideroDrawable.setMaxFrame(endFrame);
  }

  /**
   * Returns the maximum frame set by {@link #setMaxFrame(int)} or {@link #setMaxProgress(float)}
   */
  public float getMaxFrame() {
    return anideroDrawable.getMaxFrame();
  }

  /**
   * Sets the maximum progress that the animation will end at when playing or looping.
   */
  public void setMaxProgress(@FloatRange(from = 0f, to = 1f) float endProgress) {
    anideroDrawable.setMaxProgress(endProgress);
  }

  /**
   * Sets the minimum frame to the start time of the specified marker.
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinFrame(String markerName) {
    anideroDrawable.setMinFrame(markerName);
  }

  /**
   * Sets the maximum frame to the start time + duration of the specified marker.
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMaxFrame(String markerName) {
    anideroDrawable.setMaxFrame(markerName);
  }

  /**
   * Sets the minimum and maximum frame to the start time and start time + duration
   * of the specified marker.
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinAndMaxFrame(String markerName) {
    anideroDrawable.setMinAndMaxFrame(markerName);
  }

  /**
   * Sets the minimum and maximum frame to the start marker start and the maximum frame to the end marker start.
   * playEndMarkerStartFrame determines whether or not to play the frame that the end marker is on. If the end marker
   * represents the end of the section that you want, it should be true. If the marker represents the beginning of the
   * next section, it should be false.
   *
   * @throws IllegalArgumentException if either marker is not found.
   */
  public void setMinAndMaxFrame(final String startMarkerName, final String endMarkerName, final boolean playEndMarkerStartFrame) {
    anideroDrawable.setMinAndMaxFrame(startMarkerName, endMarkerName, playEndMarkerStartFrame);
  }

  /**
   * @see #setMinFrame(int)
   * @see #setMaxFrame(int)
   */
  public void setMinAndMaxFrame(int minFrame, int maxFrame) {
    anideroDrawable.setMinAndMaxFrame(minFrame, maxFrame);
  }

  /**
   * @see #setMinProgress(float)
   * @see #setMaxProgress(float)
   */
  public void setMinAndMaxProgress(
      @FloatRange(from = 0f, to = 1f) float minProgress,
      @FloatRange(from = 0f, to = 1f) float maxProgress) {
    anideroDrawable.setMinAndMaxProgress(minProgress, maxProgress);
  }

  /**
   * Reverses the current animation speed. This does NOT play the animation.
   * @see #setSpeed(float)
   * @see #playAnimation()
   * @see #resumeAnimation()
   */
  public void reverseAnimationSpeed() {
    anideroDrawable.reverseAnimationSpeed();
  }

  /**
   * Sets the playback speed. If speed < 0, the animation will play backwards.
   */
  public void setSpeed(float speed) {
    anideroDrawable.setSpeed(speed);
  }

  /**
   * Returns the current playback speed. This will be < 0 if the animation is playing backwards.
   */
  public float getSpeed() {
    return anideroDrawable.getSpeed();
  }

  public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    anideroDrawable.addAnimatorUpdateListener(updateListener);
  }

  public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    anideroDrawable.removeAnimatorUpdateListener(updateListener);
  }

  public void removeAllUpdateListeners() {
    anideroDrawable.removeAllUpdateListeners();
  }

  public void addAnimatorListener(Animator.AnimatorListener listener) {
    anideroDrawable.addAnimatorListener(listener);
  }

  public void removeAnimatorListener(Animator.AnimatorListener listener) {
    anideroDrawable.removeAnimatorListener(listener);
  }

  public void removeAllAnimatorListeners() {
    anideroDrawable.removeAllAnimatorListeners();
  }

  /**
   * @see #setRepeatCount(int)
   */
  @Deprecated
  public void loop(boolean loop) {
    anideroDrawable.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
  }

  /**
   * Defines what this animation should do when it reaches the end. This
   * setting is applied only when the repeat count is either greater than
   * 0 or {@link AnideroDrawable#INFINITE}. Defaults to {@link AnideroDrawable#RESTART}.
   *
   * @param mode {@link AnideroDrawable#RESTART} or {@link AnideroDrawable#REVERSE}
   */
  public void setRepeatMode(@AnideroDrawable.RepeatMode int mode) {
    anideroDrawable.setRepeatMode(mode);
  }

  /**
   * Defines what this animation should do when it reaches the end.
   *
   * @return either one of {@link AnideroDrawable#REVERSE} or {@link AnideroDrawable#RESTART}
   */
  @AnideroDrawable.RepeatMode
  public int getRepeatMode() {
    return anideroDrawable.getRepeatMode();
  }

  /**
   * Sets how many times the animation should be repeated. If the repeat
   * count is 0, the animation is never repeated. If the repeat count is
   * greater than 0 or {@link AnideroDrawable#INFINITE}, the repeat mode will be taken
   * into account. The repeat count is 0 by default.
   *
   * @param count the number of times the animation should be repeated
   */
  public void setRepeatCount(int count) {
    anideroDrawable.setRepeatCount(count);
  }

  /**
   * Defines how many times the animation should repeat. The default value
   * is 0.
   *
   * @return the number of times the animation should repeat, or {@link AnideroDrawable#INFINITE}
   */
  public int getRepeatCount() {
    return anideroDrawable.getRepeatCount();
  }

  public boolean isAnimating() {
    return anideroDrawable.isAnimating();
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   *
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   *
   * Be wary if you are using many images, however. Anidero is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/anidero for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
   */
  public void setImageAssetsFolder(String imageAssetsFolder) {
    anideroDrawable.setImagesAssetsFolder(imageAssetsFolder);
  }

  @Nullable
  public String getImageAssetsFolder() {
    return anideroDrawable.getImageAssetsFolder();
  }

  /**
   * Allows you to modify or clear a bitmap that was loaded for an image either automatically
   * through {@link #setImageAssetsFolder(String)} or with an {@link ImageAssetDelegate}.
   *
   * @return the previous Bitmap or null.
   */
  @Nullable
  public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
    return anideroDrawable.updateBitmap(id, bitmap);
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Anidero
   * will defer the loading of the bitmap to this delegate.
   *
   * Be wary if you are using many images, however. Anidero is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/anidero for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
   */
  public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
    anideroDrawable.setImageAssetDelegate(assetDelegate);
  }

  /**
   * Use this to manually set fonts.
   */
  public void setFontAssetDelegate(
      @SuppressWarnings("NullableProblems") FontAssetDelegate assetDelegate) {
    anideroDrawable.setFontAssetDelegate(assetDelegate);
  }

  /**
   * Set this to replace animation text with custom text at runtime
   */
  public void setTextDelegate(TextDelegate textDelegate) {
    anideroDrawable.setTextDelegate(textDelegate);
  }

  /**
   * Takes a {@link KeyPath}, potentially with wildcards or globstars and resolve it to a list of
   * zero or more actual {@link KeyPath Keypaths} that exist in the current animation.
   *
   * If you want to set value callbacks for any of these values, it is recommended to use the
   * returned {@link KeyPath} objects because they will be internally resolved to their content
   * and won't trigger a tree walk of the animation contents when applied.
   */
  public List<KeyPath> resolveKeyPath(KeyPath keyPath) {
    return anideroDrawable.resolveKeyPath(keyPath);
  }

  /**
   * Add a property callback for the specified {@link KeyPath}. This {@link KeyPath} can resolve
   * to multiple contents. In that case, the callback's value will apply to all of them.
   *
   * Internally, this will check if the {@link KeyPath} has already been resolved with
   * {@link #resolveKeyPath(KeyPath)} and will resolve it if it hasn't.
   */
  public <T> void addValueCallback(KeyPath keyPath, T property, AnideroValueCallback<T> callback) {
    anideroDrawable.addValueCallback(keyPath, property, callback);
  }

  /**
   * Overload of {@link #addValueCallback(KeyPath, Object, AnideroValueCallback)} that takes an interface. This allows you to use a single abstract
   * method code block in Kotlin such as:
   * animationView.addValueCallback(yourKeyPath, AnideroProperty.COLOR) { yourColor }
   */
  public <T> void addValueCallback(KeyPath keyPath, T property,
      final SimpleAnideroValueCallback<T> callback) {
    anideroDrawable.addValueCallback(keyPath, property, new AnideroValueCallback<T>() {
      @Override public T getValue(AnideroFrameInfo<T> frameInfo) {
        return callback.getValue(frameInfo);
      }
    });
  }

  /**
   * Set the scale on the current composition. The only cost of this function is re-rendering the
   * current frame so you may call it frequent to scale something up or down.
   *
   * The smaller the animation is, the better the performance will be. You may find that scaling an
   * animation down then rendering it in a larger ImageView and letting ImageView scale it back up
   * with a scaleType such as centerInside will yield better performance with little perceivable
   * quality loss.
   *
   * You can also use a fixed view width/height in conjunction with the normal ImageView
   * scaleTypes centerCrop and centerInside.
   */
  public void setScale(float scale) {
    anideroDrawable.setScale(scale);
    if (getDrawable() == anideroDrawable) {
      setImageDrawable(null);
      setImageDrawable(anideroDrawable);
    }
  }

  public float getScale() {
    return anideroDrawable.getScale();
  }

  @Override public void setScaleType(ScaleType scaleType) {
    super.setScaleType(scaleType);
    if (anideroDrawable != null) {
      anideroDrawable.setScaleType(scaleType);
    }
  }

  @MainThread
  public void cancelAnimation() {
    wasAnimatingWhenDetached = false;
    wasAnimatingWhenNotShown = false;
    playAnimationWhenShown = false;
    anideroDrawable.cancelAnimation();
    enableOrDisableHardwareLayer();
  }

  @MainThread
  public void pauseAnimation() {
    autoPlay = false;
    wasAnimatingWhenDetached = false;
    wasAnimatingWhenNotShown = false;
    playAnimationWhenShown = false;
    anideroDrawable.pauseAnimation();
    enableOrDisableHardwareLayer();
  }

  /**
   * Sets the progress to the specified frame.
   * If the composition isn't set yet, the progress will be set to the frame when
   * it is.
   */
  public void setFrame(int frame) {
    anideroDrawable.setFrame(frame);
  }

  /**
   * Get the currently rendered frame.
   */
  public int getFrame() {
    return anideroDrawable.getFrame();
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    anideroDrawable.setProgress(progress);
  }

  @FloatRange(from = 0.0f, to = 1.0f) public float getProgress() {
    return anideroDrawable.getProgress();
  }

  public long getDuration() {
    return composition != null ? (long) composition.getDuration() : 0;
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    anideroDrawable.setPerformanceTrackingEnabled(enabled);
  }

  @Nullable
  public PerformanceTracker getPerformanceTracker() {
    return anideroDrawable.getPerformanceTracker();
  }

  private void clearComposition() {
    composition = null;
    anideroDrawable.clearComposition();
  }

  /**
   * If you are experiencing a device specific crash that happens during drawing, you can set this to true
   * for those devices. If set to true, draw will be wrapped with a try/catch which will cause Anidero to
   * render an empty frame rather than crash your app.
   *
   * Ideally, you will never need this and the vast majority of apps and animations won't. However, you may use
   * this for very specific cases if absolutely necessary.
   *
   * There is no XML attr for this because it should be set programmatically and only for specific devices that
   * are known to be problematic.
   */
  public void setSafeMode(boolean safeMode) {
    anideroDrawable.setSafeMode(safeMode);
  }

  /**
   * If rendering via software, Android will fail to generate a bitmap if the view is too large. Rather than displaying
   * nothing, fallback on hardware acceleration which may incur a performance hit.
   *
   * @see #setRenderMode(RenderMode)
   * @see AnideroDrawable#draw(android.graphics.Canvas)
   */
  @Override
  public void buildDrawingCache(boolean autoScale) {
    Anidero.beginSection("buildDrawingCache");
    buildDrawingCacheDepth++;
    super.buildDrawingCache(autoScale);
    if (buildDrawingCacheDepth == 1 && getWidth() > 0 && getHeight() > 0 &&
        getLayerType() == LAYER_TYPE_SOFTWARE && getDrawingCache(autoScale) == null) {
      setRenderMode(HARDWARE);
    }
    buildDrawingCacheDepth--;
    Anidero.endSection("buildDrawingCache");
  }

  /**
   * Call this to set whether or not to render with hardware or software acceleration.
   * Anidero defaults to Automatic which will use hardware acceleration unless:
   * 1) There are dash paths and the device is pre-Pie.
   * 2) There are more than 4 masks and mattes and the device is pre-Pie.
   *    Hardware acceleration is generally faster for those devices unless
   *    there are many large mattes and masks in which case there is a ton
   *    of GPU uploadTexture thrashing which makes it much slower.
   *
   * In most cases, hardware rendering will be faster, even if you have mattes and masks.
   * However, if you have multiple mattes and masks (especially large ones) then you
   * should test both render modes. You should also test on pre-Pie and Pie+ devices
   * because the underlying rendering enginge changed significantly.
   */
  public void setRenderMode(RenderMode renderMode) {
    this.renderMode = renderMode;
    enableOrDisableHardwareLayer();
  }

  /**
   * Sets whether to apply opacity to the each layer instead of shape.
   * <p>
   * Opacity is normally applied directly to a shape. In cases where translucent shapes overlap, applying opacity to a layer will be more accurate
   * at the expense of performance.
   * <p>
   * The default value is false.
   * <p>
   * Note: This process is very expensive. The performance impact will be reduced when hardware acceleration is enabled.
   *
   * @see #setRenderMode(RenderMode)
   */
  public void setApplyingOpacityToLayersEnabled(boolean isApplyingOpacityToLayersEnabled) {
    anideroDrawable.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled);
  }

  /**
   * Disable the extraScale mode in {@link #draw(Canvas)} function when scaleType is FitXY. It doesn't affect the rendering with other scaleTypes.
   *
   * <p>When there are 2 animation layout side by side, the default extra scale mode might leave 1 pixel not drawn between 2 animation, and
   * disabling the extraScale mode can fix this problem</p>
   *
   * <b>Attention:</b> Disable the extra scale mode can downgrade the performance and may lead to larger memory footprint. Please only disable this
   * mode when using animation with a reasonable dimension (smaller than screen size).
   *
   * @see AnideroDrawable#drawWithNewAspectRatio(Canvas)
   */
  public void disableExtraScaleModeInFitXY() {
    anideroDrawable.disableExtraScaleModeInFitXY();
  }

  private void enableOrDisableHardwareLayer() {
    int layerType = LAYER_TYPE_SOFTWARE;
    switch (renderMode) {
      case HARDWARE:
        layerType = LAYER_TYPE_HARDWARE;
        break;
      case SOFTWARE:
        layerType = LAYER_TYPE_SOFTWARE;
        break;
      case AUTOMATIC:
        boolean useHardwareLayer = true;
        if (composition != null && composition.hasDashPattern() && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
          useHardwareLayer = false;
        } else if (composition != null && composition.getMaskAndMatteCount() > 4) {
          useHardwareLayer = false;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          useHardwareLayer = false;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
          useHardwareLayer = false;
        }
        layerType = useHardwareLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_SOFTWARE;
        break;
    }
    if (layerType != getLayerType()) {
      setLayerType(layerType, null);
    }
  }

  public boolean addAnideroOnCompositionLoadedListener(@NonNull AnideroOnCompositionLoadedListener anideroOnCompositionLoadedListener) {
    AnideroComposition composition = this.composition;
    if (composition != null) {
      anideroOnCompositionLoadedListener.onCompositionLoaded(composition);
    }
    return anideroOnCompositionLoadedListeners.add(anideroOnCompositionLoadedListener);
  }

  public boolean removeAnideroOnCompositionLoadedListener(@NonNull AnideroOnCompositionLoadedListener anideroOnCompositionLoadedListener) {
    return anideroOnCompositionLoadedListeners.remove(anideroOnCompositionLoadedListener);
  }

  public void removeAllAnideroOnCompositionLoadedListener() {
    anideroOnCompositionLoadedListeners.clear();
  }

  private static class SavedState extends BaseSavedState {
    String animationName;
    int animationResId;
    float progress;
    boolean isAnimating;
    String imageAssetsFolder;
    int repeatMode;
    int repeatCount;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      animationName = in.readString();
      progress = in.readFloat();
      isAnimating = in.readInt() == 1;
      imageAssetsFolder = in.readString();
      repeatMode = in.readInt();
      repeatCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(animationName);
      out.writeFloat(progress);
      out.writeInt(isAnimating ? 1 : 0);
      out.writeString(imageAssetsFolder);
      out.writeInt(repeatMode);
      out.writeInt(repeatCount);
    }

    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
