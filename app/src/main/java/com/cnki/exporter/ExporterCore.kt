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

                val srcFile = File(SOURCE_XML)
                if (!srcFile.exists()) {
                    XposedBridge.log("$TAG Source XML file not found at: $SOURCE_XML")
                    return@Thread
                }

                // Pure Java file I/O, no su: this code runs inside the target
                // app's process, so a su call would make the CNKI app itself
                // request root. Reading the app's own shared_prefs works
                // directly, and the target dir is made writable by the
                // companion switcher app (root) beforehand.
                val targetDir = File(ROOT_DIR, username)
                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    XposedBridge.log("$TAG Failed to create directory: ${targetDir.absolutePath}")
                    return@Thread
                }

                val targetFile = File(targetDir, "cnki_mobile.xml")
                srcFile.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                // Owner-only read/write (600): root can still read it, other apps cannot.
                targetFile.setReadable(false, false)
                targetFile.setReadable(true, true)
                targetFile.setWritable(false, false)
                targetFile.setWritable(true, true)
                targetFile.setExecutable(false, false)

                lastProcessedUser = username
                XposedBridge.log("$TAG Successfully exported XML for user [$username] to path: ${targetFile.absolutePath}")
            } catch (e: Exception) {
                XposedBridge.log("$TAG Exception during export execution: ${e.message}")
            } finally {
                isExecuting.set(false)
            }
        }.start()
    }
}
