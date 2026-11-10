package dev.example.cnkilsposed

import de.robv.android.xposed.XposedBridge

internal object XposedLog {
    fun info(message: String) {
        XposedBridge.log("[CNKI-LSPOSED] $message")
    }
}
