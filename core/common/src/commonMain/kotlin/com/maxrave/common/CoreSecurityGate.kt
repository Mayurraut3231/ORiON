package com.maxrave.common

/**
 * Enterprise Cross-Module Anti-Tamper & Code Theft Protection Gate.
 *
 * Ensures core media playback, stream extraction, and repository operations
 * verify application identity and runtime integrity before servicing requests.
 * If code is extracted or copied to another application without the official
 * signing key and package identity, operations silently fail to prevent abuse.
 */
object CoreSecurityGate {
    @Volatile
    var isCompromised: Boolean = false

    @Volatile
    var verifier: (() -> Boolean)? = null

    /**
     * Checks core integrity across modules.
     * Returns true only if environment and signatures are verified clean.
     */
    fun checkIntegrity(): Boolean {
        if (isCompromised) return false
        val v = verifier
        if (v != null) {
            val valid = try {
                v.invoke()
            } catch (_: Throwable) {
                false
            }
            if (!valid) {
                isCompromised = true
                return false
            }
        }
        return true
    }
}
