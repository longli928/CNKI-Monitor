package com.cnki.exporter

import android.os.Handler
import android.os.Looper
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object ExporterCore {
    private const val TAG = "[CNKI-AccountExport]"
    private const val ROOT_DIR = "/storage/emulated/0/Download/CNKI_account"
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

                // Only export on a new login: if the exported copy is
                // identical to the current XML, there is nothing new to save,
                // so do not create anything just for opening the app.
                val targetDir = File(ROOT_DIR, username)
                val targetFile = File(targetDir, "cnki_mobile.xml")
                if (targetFile.exists() && sha256(targetFile) == sha256(srcFile)) {
                    lastProcessedUser = username
                    XposedBridge.log("$TAG Account [$username] already exported, skipping.")
                    return@Thread
                }

                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    XposedBridge.log("$TAG Failed to create directory: ${targetDir.absolutePath}")
                    return@Thread
                }

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

    private fun sha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
