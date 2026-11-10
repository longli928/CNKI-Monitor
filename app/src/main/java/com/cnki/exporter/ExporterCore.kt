package com.cnki.exporter

import android.os.Handler
import android.os.Looper
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object ExporterCore {
    private const val TAG = "[CNKI-AccountExport]"
    private const val ROOT_DIR = "/data/adb/cnki-accounts"
    private const val SOURCE_XML = "/data/data/com.cnki.android.cnkimobile/shared_prefs/cnki_mobile.xml"
    
    private val isExecuting = AtomicBoolean(false)
    private var lastProcessedUser: String? = null
    private val handler = Handler(Looper.getMainLooper())

    fun processUsername(username: String) {
        if (username.isEmpty() || username == "未登录") {
            return
        }

        if (username == lastProcessedUser) {
            return
        }

        if (!isExecuting.compareAndSet(false, true)) {
            XposedBridge.log("$TAG Export task already running, skipping concurrent request.")
            return
        }

        Thread {
            try {
                XposedBridge.log("$TAG Detected valid username: $username")

                val targetDir = "$ROOT_DIR/$username"
                val targetFile = "$targetDir/cnki_mobile.xml"

                val srcFile = File(SOURCE_XML)
                if (!srcFile.exists()) {
                    XposedBridge.log("$TAG Source XML file not found at: $SOURCE_XML")
                    return@Thread
                }

                val escapedDir = escapeShell(targetDir)
                val escapedSrc = escapeShell(SOURCE_XML)
                val escapedDst = escapeShell(targetFile)

                val cmd = "mkdir -p $escapedDir && cp $escapedSrc $escapedDst && chmod 644 $escapedDst"
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    lastProcessedUser = username
                    XposedBridge.log("$TAG Successfully exported XML for user [$username] to path: $targetFile")
                } else {
                    XposedBridge.log("$TAG Failed to execute root command, exit code: $exitCode")
                }
            } catch (e: Exception) {
                XposedBridge.log("$TAG Exception during export execution: ${e.message}")
            } finally {
                isExecuting.set(false)
            }
        }.start()
    }

    private fun escapeShell(arg: String): String {
        return "'" + arg.replace("'", "'\''") + "'"
    }
}
