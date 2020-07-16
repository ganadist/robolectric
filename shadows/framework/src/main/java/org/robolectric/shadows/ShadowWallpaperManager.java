package org.robolectric.shadows;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(WallpaperManager.class)
public class ShadowWallpaperManager {
  private Bitmap lockScreenImage = null;
  private Bitmap homeScreenImage = null;
  private boolean isWallpaperAllowed = true;
  private boolean isWallpaperSupported = true;

  @Implementation
  protected void sendWallpaperCommand(
      IBinder windowToken, String action, int x, int y, int z, Bundle extras) {}

  /**
   * Caches {@code fullImage} in the memory based on {@code which}.
   *
   * @param fullImage the bitmap image to be cached in the memory
   * @param visibleCropHint not used
   * @param allowBackup not used
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return 0 if fails to cache. Otherwise, 1.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  protected int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) {
    if (which == WallpaperManager.FLAG_LOCK) {
      lockScreenImage = fullImage;
      return 1;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      homeScreenImage = fullImage;
      return 1;
    }
    return 0;
  }

  /**
   * Returns the memory cached {@link Bitmap} associated with {@code which}.
   *
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}.
   * @return The memory cached {@link Bitmap} associated with {@code which}. {@code null} if no
   *     bitmap was set.
   */
  @Nullable
  public Bitmap getBitmap(int which) {
    if (which == WallpaperManager.FLAG_LOCK) {
      return lockScreenImage;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      return homeScreenImage;
    }
    return null;
  }

  /**
   * Gets a wallpaper file associated with {@code which}.
   *
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return An open, readable file descriptor to the requested wallpaper image file; {@code null}
   *     if no such wallpaper is configured.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  @Nullable
  protected ParcelFileDescriptor getWallpaperFile(int which) {
    if (which == WallpaperManager.FLAG_SYSTEM && homeScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(homeScreenImage, "home_wallpaper");
    } else if (which == WallpaperManager.FLAG_LOCK && lockScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(lockScreenImage, "lock_screen_wallpaper");
    }
    return null;
  }

  @Implementation(minSdk = VERSION_CODES.N)
  public boolean isSetWallpaperAllowed() {
    return isWallpaperAllowed;
  }

  public void setIsSetWallpaperAllowed(boolean allowed) {
    isWallpaperAllowed = allowed;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected boolean isWallpaperSupported() {
    return isWallpaperSupported;
  }

  public void setIsWallpaperSupported(boolean supported) {
    isWallpaperSupported = supported;
  }

  /**
   * Caches {@code bitmapData} in the memory based on {@code which}.
   *
   * @param bitmapData the input stream which contains a bitmap image to be cached in the memory
   * @param visibleCropHint not used
   * @param allowBackup not used
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return 0 if fails to cache. Otherwise, 1.
   */
  @Implementation(minSdk = VERSION_CODES.N)
  protected int setStream(
      InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) {
    try {
      if (which == WallpaperManager.FLAG_LOCK) {
        lockScreenImage = BitmapFactory.decodeStream(bitmapData);
        return 1;
      } else if (which == WallpaperManager.FLAG_SYSTEM) {
        homeScreenImage = BitmapFactory.decodeStream(bitmapData);
        return 1;
      }
    } finally {
      closeQuietly(bitmapData);
    }
    return 0;
  }

  /**
   * Returns an open, readable file descriptor to the given {@code image} or {@code null} if there
   * is an {@link IOException}.
   */
  private static ParcelFileDescriptor createParcelFileDescriptorFromBitmap(
      Bitmap image, String fileName) {
    File tmpFile = new File(RuntimeEnvironment.application.getCacheDir(), fileName);
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(tmpFile);
      image.compress(CompressFormat.PNG, /* quality= */ 0, fileOutputStream);
      return ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (IOException e) {
      return null;
    } finally {
      closeQuietly(fileOutputStream);
    }
  }

  public static void closeQuietly(@Nullable Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        // Do nothing.
      }
    }
  }
}
