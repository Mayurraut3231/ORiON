package com.maxrave.simpmusic.security

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import androidx.appcompat.app.AlertDialog
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.BuildConfig
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import kotlin.system.exitProcess

/**
 * Enterprise-grade Security & Anti-Tamper Engine for ORiON.
 *
 * Provides multi-layered defense-in-depth:
 * 1. App Signature & Certificate Fingerprint Verification (Anti-Repackaging / Anti-Cloning)
 * 2. Dynamic Instrumentation & Memory Hooking Detection (Anti-Frida / Anti-Xposed / Anti-LSPosed)
 * 3. Anti-Debugging & Process Trace Attachment Detection (Anti-Ptrace / Anti-JDWP)
 * 4. Runtime Integrity Verification with Obfuscated Hash Storage
 */
object SecurityGuard {

    private const val TAG = "SecurityGuard"

    // XOR mask used to decode the expected release certificate SHA-256 fingerprint
    private const val FINGERPRINT_MASK: Byte = 0x5A

    // Obfuscated representation of 36799843F6C04284E99DB6B30E169A84200326257BC0B374A04D6042B817DB7D
    // Prevents simple strings/grep extraction and smali patching
    private val OBFUSCATED_SHA256 = byteArrayOf(
        0x69.toByte(), 0x6C.toByte(), 0x6D.toByte(), 0x63.toByte(), 0x63.toByte(), 0x62.toByte(), 0x6E.toByte(), 0x69.toByte(),
        0x1C.toByte(), 0x6C.toByte(), 0x19.toByte(), 0x6A.toByte(), 0x6E.toByte(), 0x68.toByte(), 0x62.toByte(), 0x6E.toByte(),
        0x1F.toByte(), 0x63.toByte(), 0x63.toByte(), 0x1E.toByte(), 0x18.toByte(), 0x6C.toByte(), 0x18.toByte(), 0x69.toByte(),
        0x6A.toByte(), 0x1F.toByte(), 0x6B.toByte(), 0x6C.toByte(), 0x63.toByte(), 0x1B.toByte(), 0x62.toByte(), 0x6E.toByte(),
        0x68.toByte(), 0x6A.toByte(), 0x6A.toByte(), 0x69.toByte(), 0x68.toByte(), 0x6C.toByte(), 0x68.toByte(), 0x6F.toByte(),
        0x6D.toByte(), 0x18.toByte(), 0x19.toByte(), 0x6A.toByte(), 0x18.toByte(), 0x69.toByte(), 0x6D.toByte(), 0x6E.toByte(),
        0x1B.toByte(), 0x6A.toByte(), 0x6E.toByte(), 0x1E.toByte(), 0x6C.toByte(), 0x6A.toByte(), 0x6E.toByte(), 0x68.toByte(),
        0x18.toByte(), 0x62.toByte(), 0x6B.toByte(), 0x6D.toByte(), 0x1E.toByte(), 0x18.toByte(), 0x6D.toByte(), 0x1E.toByte()
    )

    private var isVerified = false

    /**
     * Initializes security checks early in Application.onCreate.
     * If running in release and critical tampering is detected, kills process immediately.
     */
    fun init(context: Context) {
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "Running in DEBUG build - security enforcement relaxed for development")
            return
        }

        if (isEnvironmentCompromised(context)) {
            terminateProcess()
        } else {
            isVerified = true
        }
    }

    /**
     * Checks security state in UI context (e.g. MainActivity.onCreate).
     * If compromised, displays a clear security alert and gracefully terminates the activity.
     */
    fun checkAndEnforce(activity: Activity): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }

        if (isEnvironmentCompromised(activity)) {
            try {
                AlertDialog.Builder(activity)
                    .setTitle("Security Violation")
                    .setMessage("App integrity check failed.\n\nThis application has been modified, re-signed, or is running in an unsafe debugging environment. ORiON will now close to protect system integrity.")
                    .setCancelable(false)
                    .setPositiveButton("Exit") { _, _ ->
                        activity.finishAffinity()
                        terminateProcess()
                    }
                    .show()
            } catch (_: Throwable) {
                activity.finishAffinity()
                terminateProcess()
            }
            return false
        }
        return true
    }

    /**
     * Checks if the runtime environment or APK has been modified or hooked.
     */
    fun isEnvironmentCompromised(context: Context): Boolean {
        // 1. Verify APK signature
        if (!verifyAppSignature(context)) {
            Logger.e(TAG, "Integrity check failed: APK signature does not match official ORiON key")
            return true
        }

        // 2. Check for active debuggers or ptrace tracing
        if (isDebuggerAttached(context)) {
            Logger.e(TAG, "Security check failed: Active debugger or ptrace detected")
            return true
        }

        // 3. Check for dynamic instrumentation (Frida, Xposed, LSPosed)
        if (isDynamicHookingDetected()) {
            Logger.e(TAG, "Security check failed: Dynamic hooking framework detected")
            return true
        }

        return false
    }

    /**
     * Verifies that the APK's certificate SHA-256 fingerprint matches the official ORiON keystore.
     */
    private fun verifyAppSignature(context: Context): Boolean {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                return false
            }

            val expectedFingerprint = getExpectedFingerprint()

            for (sig in signatures) {
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(sig.toByteArray())
                val hexString = digest.joinToString("") { "%02X".format(it) }
                if (hexString.equals(expectedFingerprint, ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Throwable) {
            Logger.e(TAG, "Error checking signature: ${e.message}")
            false
        }
    }

    /**
     * Reconstructs the official SHA-256 fingerprint dynamically using bitwise XOR.
     */
    private fun getExpectedFingerprint(): String {
        val restored = ByteArray(OBFUSCATED_SHA256.size)
        for (i in OBFUSCATED_SHA256.indices) {
            restored[i] = (OBFUSCATED_SHA256[i].toInt() xor FINGERPRINT_MASK.toInt()).toByte()
        }
        return String(restored, Charsets.UTF_8)
    }

    /**
     * Detects attached debuggers and ptrace process inspection.
     */
    private fun isDebuggerAttached(context: Context): Boolean {
        // Standard Android debug flags
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }

        // Check if application is flagged debuggable in release mode
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!BuildConfig.DEBUG && isDebuggable) {
            return true
        }

        // Read /proc/self/status for TracerPid
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists() && statusFile.canRead()) {
                val hasTracer = statusFile.bufferedReader().useLines { lines ->
                    lines.any { line ->
                        if (line.startsWith("TracerPid:", ignoreCase = true)) {
                            val pidStr = line.substringAfter(":").trim()
                            val pid = pidStr.toIntOrNull() ?: 0
                            pid > 0
                        } else {
                            false
                        }
                    }
                }
                if (hasTracer) return true
            }
        } catch (_: Throwable) {}

        return false
    }

    /**
     * Detects presence of dynamic instrumentation frameworks (Frida, Xposed, LSPosed, Substrate).
     */
    private fun isDynamicHookingDetected(): Boolean {
        try {
            // Check memory maps for injected libraries
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists() && mapsFile.canRead()) {
                val hooked = mapsFile.bufferedReader().useLines { lines ->
                    lines.any { line ->
                        val lower = line.lowercase()
                        lower.contains("frida") ||
                            lower.contains("gadget") ||
                            lower.contains("gum-js-loop") ||
                            lower.contains("xposedbridge") ||
                            lower.contains("edxp")
                    }
                }
                if (hooked) return true
            }

            // Check for known hooking engine classes in ClassLoader
            val suspiciousClasses = arrayOf(
                "de.robv.android.xposed.XposedBridge",
                "de.robv.android.xposed.XC_MethodHook",
                "com.elderdrivers.riru.edxp.core.EdXpCore",
                "me.weishu.exposed.ExposedBridge",
                "io.github.lsposed.lspd.nativebridge.NativeBridge"
            )
            for (clazz in suspiciousClasses) {
                try {
                    Class.forName(clazz)
                    return true
                } catch (_: ClassNotFoundException) {}
            }

            // Check for standard Frida server listening ports (27042, 27043)
            val fridaPorts = intArrayOf(27042, 27043)
            for (port in fridaPorts) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("127.0.0.1", port), 40)
                        return true
                    }
                } catch (_: Throwable) {}
            }

            // Check for Frida artifacts in /data/local/tmp
            val tmpDir = File("/data/local/tmp")
            if (tmpDir.exists() && tmpDir.isDirectory) {
                val files = tmpDir.list()
                if (files != null) {
                    for (f in files) {
                        if (f.contains("frida", ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
        } catch (_: Throwable) {}

        return false
    }

    /**
     * Kills the current process immediately.
     */
    private fun terminateProcess() {
        try {
            Process.killProcess(Process.myPid())
        } catch (_: Throwable) {}
        exitProcess(0)
    }
}
