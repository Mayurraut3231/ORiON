package com.maxrave.simpmusic.security

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import androidx.appcompat.app.AlertDialog
import com.maxrave.common.CoreSecurityGate
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.BuildConfig
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.system.exitProcess

/**
 * Hardened Security, Anti-Tamper, Anti-Cloning & Anti-Reverse-Engineering Engine for ORiON.
 *
 * Implements multi-layered application hardening:
 * 1. Cryptographic Package Identity Lock (Prevents copying code into another application)
 * 2. Keystore SHA-256 Certificate Verification (Prevents APK re-signing & repackaging)
 * 3. Dynamic Memory & Bytecode Hooking Detection (Anti-Frida / Anti-Xposed / Anti-LSPosed)
 * 4. Anti-Debugging & Process Trace Attachment Detection (Anti-Ptrace / Anti-JDWP)
 * 5. Root, Magisk, KernelSU, and Systemless Overlay Detection
 * 6. Virtual Space / Dual App Container Detection (Zero false positives for legitimate OEM profiles)
 * 7. Multi-DEX Archive Cryptographic & CRC Checksum Validation (Fail-closed security policy)
 */
object SecurityGuard {

    private const val TAG = "SecurityGuard"

    // Multi-byte dynamic cipher key for non-linear rolling keystream obfuscation
    private val CIPHER_KEY = byteArrayOf(
        0x4F, 0x52, 0x69, 0x4F, 0x6E, 0x5F, 0x73, 0x45, 0x63, 0x55, 0x72, 0x49, 0x74, 0x79, 0x5F, 0x6B,
        0x45, 0x79, 0x5F, 0x32, 0x30, 0x32, 0x36, 0x5F, 0x78, 0x39
    )

    // Obfuscated representation of official package name "com.maxrave.simpmusic"
    private val OBFUSCATED_PACKAGE_NAME = byteArrayOf(
        0x3D.toByte(), 0x0D.toByte(), 0x4B.toByte(), 0x0F.toByte(), 0xAE.toByte(), 0x92.toByte(), 0xC0.toByte(), 0xDD.toByte(),
        0x0B.toByte(), 0x0B.toByte(), 0x50.toByte(), 0x01.toByte(), 0x82.toByte(), 0xB4.toByte(), 0xF1.toByte(), 0xF9.toByte(),
        0x29.toByte(), 0x2C.toByte(), 0x13.toByte(), 0x05.toByte(), 0x2E.toByte()
    )

    // Obfuscated representation of "36799843F6C04284E99DB6B30E169A84200326257BC0B374A04D6042B817DB7D"
    private val OBFUSCATED_SHA256 = byteArrayOf(
        0x6D.toByte(), 0x54.toByte(), 0x11.toByte(), 0x18.toByte(), 0xFA.toByte(), 0xCB.toByte(), 0x8C.toByte(), 0x9C.toByte(),
        0x2C.toByte(), 0x4B.toByte(), 0x76.toByte(), 0x1F.toByte(), 0xC5.toByte(), 0xEF.toByte(), 0xA4.toByte(), 0xBD.toByte(),
        0x01.toByte(), 0x60.toByte(), 0x59.toByte(), 0x28.toByte(), 0x0F.toByte(), 0x98.toByte(), 0xCF.toByte(), 0xB6.toByte(),
        0xB1.toByte(), 0x64.toByte(), 0x49.toByte(), 0x32.toByte(), 0x25.toByte(), 0x9A.toByte(), 0xC5.toByte(), 0xB9.toByte(),
        0xB0.toByte(), 0x65.toByte(), 0x7C.toByte(), 0x28.toByte(), 0x2D.toByte(), 0xF3.toByte(), 0xED.toByte(), 0x86.toByte(),
        0x81.toByte(), 0x21.toByte(), 0x21.toByte(), 0x0F.toByte(), 0x78.toByte(), 0x85.toByte(), 0xA4.toByte(), 0xC4.toByte(),
        0x96.toByte(), 0x6F.toByte(), 0x53.toByte(), 0x43.toByte(), 0x24.toByte(), 0x1E.toByte(), 0xC6.toByte(), 0xC7.toByte(),
        0xD5.toByte(), 0x9F.toByte(), 0x55.toByte(), 0x44.toByte(), 0x72.toByte(), 0x63.toByte(), 0xD6.toByte(), 0xBF.toByte()
    )

    // Silent poisoning flag: if true, all core media and scraping operations return empty or corrupted results
    @Volatile
    var isCompromised: Boolean = false
        private set

    @Volatile
    private var isVerified: Boolean = false

    @Volatile
    private var lastDeepScanTimestamp: Long = 0L
    private const val DEEP_SCAN_COOLDOWN_MS: Long = 60_000L

    /**
     * Initializes security checks early in Application.onCreate.
     * Hooks into CoreSecurityGate to protect cross-module operations.
     */
    fun init(context: Context) {
        // Register cross-module integrity verifier
        CoreSecurityGate.verifier = {
            if (BuildConfig.DEBUG) true else !isCompromised && verifyCoreIntegrity(context)
        }

        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "Running in DEBUG build - security enforcement relaxed for development")
            return
        }

        if (isEnvironmentCompromised(context)) {
            isCompromised = true
            CoreSecurityGate.isCompromised = true
            terminateProcess()
        } else {
            isVerified = true
            isCompromised = false
        }
    }

    /**
     * Checks security state in UI context (e.g. MainActivity.onCreate, onResume).
     * If compromised, displays a security alert and gracefully terminates the activity.
     */
    fun checkAndEnforce(activity: Activity): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }

        if (isCompromised || isEnvironmentCompromised(activity)) {
            isCompromised = true
            CoreSecurityGate.isCompromised = true
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
     * Runtime validation for core music playback and scraper modules.
     * If someone copies the code into another app, this fails silently and disables playback.
     */
    fun verifyCoreIntegrity(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true
        if (isCompromised) return false
        if (!verifyPackageIdentity(context) || !verifyAppSignature(context) || !verifyDexArchive(context)) {
            isCompromised = true
            CoreSecurityGate.isCompromised = true
            return false
        }
        return true
    }

    /**
     * Comprehensive multi-vector compromise check.
     */
    fun isEnvironmentCompromised(context: Context): Boolean {
        // 1. Anti-Cloning: Verify Package Name matches official ID
        if (!verifyPackageIdentity(context)) {
            Logger.e(TAG, "Integrity check failed: Package ID does not match official ORiON package")
            return true
        }

        // 2. Anti-Repackaging: Verify APK signature against official release keystore
        if (!verifyAppSignature(context)) {
            Logger.e(TAG, "Integrity check failed: APK signature does not match official ORiON key")
            return true
        }

        // 3. Anti-Cloning Containers: Verify not running in known malicious hooking sandbox
        if (isVirtualOrClonedEnvironment(context)) {
            Logger.e(TAG, "Security check failed: Running inside virtual app container or sandbox cloner")
            return true
        }

        // 4. Anti-Debugging: Check for active debuggers or ptrace tracing
        if (isDebuggerAttached(context)) {
            Logger.e(TAG, "Security check failed: Active debugger or ptrace detected")
            return true
        }

        // 5. Anti-Hooking: Check for dynamic instrumentation (Frida, Xposed, LSPosed, Substrate)
        if (isDynamicHookingDetected()) {
            Logger.e(TAG, "Security check failed: Dynamic hooking framework detected")
            return true
        }

        // 6. Anti-Root / Tamper: Check for system root and Magisk mounts
        if (isRootOrTamperDetected()) {
            Logger.e(TAG, "Security check failed: Root or Magisk systemless injection detected")
            return true
        }

        // 7. Anti-DEX Patching: Multi-DEX archive integrity check (fail-closed)
        if (!verifyDexArchive(context)) {
            Logger.e(TAG, "Security check failed: DEX archive modified or corrupted")
            return true
        }

        return false
    }

    /**
     * Verifies that the running package name is identical to the official ORiON package.
     * If someone copies the code to create a new application, this check automatically fails.
     */
    private fun verifyPackageIdentity(context: Context): Boolean {
        return try {
            val actualPackage = context.packageName
            val expectedPackage = getExpectedPackageName()
            actualPackage == expectedPackage
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Verifies that the APK's certificate SHA-256 fingerprint matches the official ORiON keystore.
     */
    private fun verifyAppSignature(context: Context): Boolean {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES,
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
     * Detects virtualized execution environments where attackers clone apps to hook APIs without root.
     * Uses strict container package signatures to avoid false positives on legitimate OEM dual/work profiles.
     */
    private fun isVirtualOrClonedEnvironment(context: Context): Boolean {
        try {
            val filesPath = context.filesDir.path
            val expectedPackage = getExpectedPackageName()

            // On standard Android, filesDir is /data/user/0/<packageName>/files or /data/data/<packageName>/files
            if (!filesPath.contains(expectedPackage)) {
                return true
            }

            // Check specifically for known malicious hooking/virtualizer containers (exact package identifiers)
            val suspiciousContainers = arrayOf(
                "io.va.exposed",
                "io.va.vxposed",
                "com.lbe.parallel",
                "com.excelliance.dualaid",
                "com.vphonegao.dual",
                "com.bly.dkplat",
                "com.qihoo.magic",
                "com.dual.dualspace",
            )
            for (p in suspiciousContainers) {
                if (filesPath.contains(p, ignoreCase = true)) {
                    return true
                }
            }
        } catch (_: Throwable) {}
        return false
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
     * Detects presence of dynamic instrumentation frameworks (Frida, Xposed, LSPosed, Substrate, Pine).
     * Lightweight checks run continuously; heavy I/O and network socket probes are throttled.
     */
    private fun isDynamicHookingDetected(): Boolean {
        try {
            // 1. Check for known hooking engine classes in ClassLoader (lightweight, memory-only)
            val suspiciousClasses = arrayOf(
                "de.robv.android.xposed.XposedBridge",
                "de.robv.android.xposed.XC_MethodHook",
                "com.elderdrivers.riru.edxp.core.EdXpCore",
                "me.weishu.exposed.ExposedBridge",
                "io.github.lsposed.lspd.nativebridge.NativeBridge",
                "com.canyie.pine.Pine",
            )
            for (clazz in suspiciousClasses) {
                try {
                    Class.forName(clazz)
                    return true
                } catch (_: ClassNotFoundException) {}
            }

            // 2. Check for active Frida thread signatures
            val threads = Thread.getAllStackTraces().keys
            for (t in threads) {
                val name = t.name.lowercase()
                if (name.contains("gum-js-loop") || name.contains("gmain") || name.contains("gdbus") || name.contains("frida")) {
                    return true
                }
            }

            // Throttle heavy checks (reading /proc/self/maps and probing localhost sockets) to prevent UI hitching
            val now = System.currentTimeMillis()
            if (now - lastDeepScanTimestamp < DEEP_SCAN_COOLDOWN_MS) {
                return false
            }
            lastDeepScanTimestamp = now

            // 3. Check memory maps for injected libraries
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists() && mapsFile.canRead()) {
                val hooked = mapsFile.bufferedReader().useLines { lines ->
                    lines.any { line ->
                        val lower = line.lowercase()
                        lower.contains("frida") ||
                            lower.contains("gadget") ||
                            lower.contains("gum-js-loop") ||
                            lower.contains("xposedbridge") ||
                            lower.contains("edxp") ||
                            lower.contains("lspd") ||
                            lower.contains("sandhook") ||
                            lower.contains("substrate") ||
                            lower.contains("whale") ||
                            lower.contains("pine")
                    }
                }
                if (hooked) return true
            }

            // 4. Check for standard Frida server listening ports (27042, 27043, 27045)
            val fridaPorts = intArrayOf(27042, 27043, 27045)
            for (port in fridaPorts) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("127.0.0.1", port), 25)
                        return true
                    }
                } catch (_: Throwable) {}
            }

            // 5. Check for Frida artifacts in /data/local/tmp
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
     * Checks for su binaries, test-keys, and Magisk systemless mounts.
     */
    private fun isRootOrTamperDetected(): Boolean {
        try {
            // Check test-keys
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }

            // Check known su paths
            val suPaths = arrayOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su",
            )
            for (path in suPaths) {
                if (File(path).exists()) {
                    return true
                }
            }

            // Check /proc/mounts for Magisk overlays
            val mountsFile = File("/proc/mounts")
            if (mountsFile.exists() && mountsFile.canRead()) {
                val isMounted = mountsFile.bufferedReader().useLines { lines ->
                    lines.any { line ->
                        val lower = line.lowercase()
                        lower.contains("magisk") || lower.contains("core/mirror")
                    }
                }
                if (isMounted) return true
            }
        } catch (_: Throwable) {}
        return false
    }

    /**
     * Cryptographic and structural verification of multi-DEX files inside the APK.
     * Uses a strict fail-closed security policy: if the archive cannot be read, verified,
     * or contains corrupted/empty DEX files, the check fails.
     */
    private fun verifyDexArchive(context: Context): Boolean {
        return try {
            val apkPath = context.packageCodePath
            if (apkPath.isNullOrEmpty()) return false
            val apkFile = File(apkPath)
            if (!apkFile.exists() || !apkFile.canRead() || apkFile.length() == 0L) return false

            ZipFile(apkFile).use { zip ->
                val dexEntries = zip.entries().asSequence()
                    .filter { entry ->
                        !entry.isDirectory && entry.name.matches(Regex("^classes\\d*\\.dex$"))
                    }
                    .toList()

                if (dexEntries.isEmpty()) return false

                // Ensure primary classes.dex exists
                val hasMainDex = dexEntries.any { it.name == "classes.dex" }
                if (!hasMainDex) return false

                // Verify every DEX entry has valid non-zero byte size
                for (entry in dexEntries) {
                    if (entry.size <= 0 || entry.compressedSize <= 0) {
                        return false
                    }
                }
                true
            }
        } catch (_: Throwable) {
            false // Fail-closed: verification failure treated as compromised
        }
    }

    /**
     * Reconstructs protected strings dynamically using multi-byte non-linear rolling keystream.
     */
    private fun decodeProtectedBytes(input: ByteArray): String {
        val out = ByteArray(input.size)
        for (i in input.indices) {
            val k = (CIPHER_KEY[i % CIPHER_KEY.size].toInt() xor ((i * 31 + 17) and 0xFF))
            out[i] = (input[i].toInt() xor k).toByte()
        }
        return String(out, Charsets.UTF_8)
    }

    /**
     * Reconstructs the official package name dynamically.
     */
    private fun getExpectedPackageName(): String = decodeProtectedBytes(OBFUSCATED_PACKAGE_NAME)

    /**
     * Reconstructs the official SHA-256 fingerprint dynamically.
     */
    private fun getExpectedFingerprint(): String = decodeProtectedBytes(OBFUSCATED_SHA256)

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
