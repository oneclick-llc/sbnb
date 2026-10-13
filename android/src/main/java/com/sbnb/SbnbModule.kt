package com.sbnb

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import com.facebook.react.bridge.ColorPropConverter
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.PixelUtil

class SbnbModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

  init {
    reactContext.addLifecycleEventListener(this)
  }

  override fun getName(): String {
    return NAME
  }

  var isFitsSystemWindows = true

  // The last values JS applied, re-asserted on host resume: a write that
  // races a null currentActivity (e.g. mid-backgrounding) is silently
  // dropped here, and other writers (window-theme re-applies, other
  // activities) can stomp the system-bar state while the host is paused.
  // Re-applying on resume makes the JS-owned state self-healing —
  // intermittent "stuck" icon tones fix themselves on the next foreground.
  // (Looky-8849)
  // @Volatile: written on the NativeModules thread, read on the UI thread in
  // onHostResume — guarantees the resume re-assert sees the latest JS value.
  @Volatile private var lastStatusBarStyleDark: Boolean? = null
  @Volatile private var lastSystemUIColor: Double? = null

  override fun onHostResume() {
    lastSystemUIColor?.let { applySystemUIColor(it) }
    lastStatusBarStyleDark?.let { applyStatusBarStyle(it) }
  }

  override fun onHostPause() {}

  override fun onHostDestroy() {}

  override fun invalidate() {
    reactApplicationContext.removeLifecycleEventListener(this)
    super.invalidate()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun statusBarHeight(): Double {
    return PixelUtil.toDIPFromPixel(reactApplicationContext.currentActivity!!.statusBarHeight.toFloat()).toDouble()
  }
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun navigationBarHeight(): Double {
    if (isFitsSystemWindows) return 0.0
    return PixelUtil.toDIPFromPixel(reactApplicationContext.currentActivity!!.navigationBarHeight.toFloat()).toDouble()
  }

  @ReactMethod
  fun setStatusBarStyle(dark: Boolean) {
    lastStatusBarStyleDark = dark
    applyStatusBarStyle(dark)
  }

  private fun applyStatusBarStyle(dark: Boolean) {
    reactApplicationContext.currentActivity?.runOnUiThread {
      val activity = reactApplicationContext.currentActivity ?: return@runOnUiThread
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@runOnUiThread
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.decorView.windowInsetsController?.setSystemBarsAppearance(
          if (dark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
          WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        );
      } else {
        if (!dark) {
          // Draw light icons on a dark background color
          activity.window.decorView.systemUiVisibility =
            activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
          // Draw dark icons on a light background color
          activity.window.decorView.systemUiVisibility =
            activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
      }
      // Mirror the icon tone onto the navigation bar. Nothing else in the app
      // writes APPEARANCE_LIGHT_NAVIGATION_BARS after enableEdgeToEdge() seeds
      // it from the SYSTEM theme at cold start, so without this the nav-bar
      // glyphs keep that stale tone across in-app theme switches — visible on
      // Android 15+, where window bar-color painting is ignored and the bar is
      // genuinely transparent. The extension handles R+ via the insets
      // controller and O..Q via SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; the
      // status-bar and navigation-bar appearance masks are disjoint.
      activity.window.setNavigationBarDarkIcons(dark)
    }
  }

  @ReactMethod
  fun setSystemUIColor(color: Double) {
    lastSystemUIColor = color
    applySystemUIColor(color)
  }

  private fun applySystemUIColor(color: Double) {
    reactApplicationContext.currentActivity?.runOnUiThread {
      reactApplicationContext.currentActivity?.let {
        val newColor = ColorPropConverter.getColor(color, it) ?: Color.MAGENTA
        it.window.statusBarColor = newColor
        if (isFitsSystemWindows || it.systemNavigationMode != SystemNavigationMode.GESTURE) {
          it.window.navigationBarColor = newColor
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          it.window?.isNavigationBarContrastEnforced = false
        }
      }
    }
  }

  @ReactMethod
  fun toggleFitsSystemWindows(isEnabled: Boolean) {
    //currentActivity?.runOnUiThread {
    //  currentActivity?.let {
    //    isFitsSystemWindows = isEnabled
    //    WindowCompat.setDecorFitsSystemWindows(it.window, isFitsSystemWindows)
    //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    //      it.window?.isNavigationBarContrastEnforced = false
    //    }
    //    if (it.systemNavigationMode == SystemNavigationMode.GESTURE) {
    //      it.window.navigationBarColor = Color.TRANSPARENT
    //    }
    //    if (isFitsSystemWindows) {
    //      it.window.navigationBarColor = it.window.statusBarColor
    //    }
    //  }
    //}
  }

  companion object {
    const val NAME = "Sbnb"
  }
}
