package com.cnki.exporter

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookMain : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "[CNKI-AccountExport]"
        private const val TARGET_PACKAGE = "com.cnki.android.cnkimobile"
        private const val VIEW_ID_NAME = "person_name"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return

        XposedBridge.log("$TAG Target package loaded: ${lpparam.packageName}")

        // Hook android.app.Activity.onResume instead of a specific activity:
        // the CNKI app has no com.cnki.android.cnkimobile.activity.MainActivity
        // class, so hook every activity and let the person_name lookup below
        // pick the one that actually shows the username.
        XposedHelpers.findAndHookMethod(
            "android.app.Activity",
            lpparam.classLoader,
            "onResume",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val activity = param.thisObject
                    try {
                        val resources = (activity as android.app.Activity).resources
                        val resId = resources.getIdentifier(VIEW_ID_NAME, "id", TARGET_PACKAGE)
                        if (resId != 0) {
                            val textView = activity.findViewById<android.widget.TextView>(resId)
                            if (textView != null) {
                                textView.postDelayed({
                                    try {
                                        val currentText = textView.text?.toString()?.trim() ?: ""
                                        ExporterCore.processUsername(currentText)
                                    } catch (e: Exception) {
                                        XposedBridge.log("$TAG Error reading TextView text: ${e.message}")
                                    }
                                }, 1500)
                            }
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("$TAG Hook onResume failed: ${e.message}")
                    }
                }
            }
        )
    }
}
