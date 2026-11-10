package dev.example.cnkilsposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Safe starter entry point.
 *
 * It only confirms that the target app process was loaded. It deliberately
 * does not inspect private app data, read SharedPreferences/XML, collect
 * authentication material, or alter the target app's state.
 */
class CnkiEntry : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return

        // Keep this starter intentionally minimal and free of account/session data.
        XposedLog.info("Target process loaded: $TARGET_PACKAGE")
    }

    private companion object {
        const val TARGET_PACKAGE = "com.cnki.android.cnkimobile"
    }
}
