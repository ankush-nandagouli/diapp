package com.example.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Result data class for password validation.
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Security utilities for Dakshyam Innovations.
 * Provides cryptographic hashing, salt generation, brute-force rate-limiting,
 * session token generation, and password policy validation.
 */
object SecurityUtils {

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 60_000L // 60 seconds lockout

    // Track failed attempts per partner email or ID: key -> (failedCount, lockoutUntilTimestamp)
    private val failedAttemptsTracker = ConcurrentHashMap<String, Pair<Int, Long>>()

    /**
     * Generates a random cryptographic salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA256 with a unique salt.
     */
    fun hashPassword(password: String, salt: String): String {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (_: Exception) {
            sha256WithSalt(password, salt)
        }
    }

    /**
     * Creates a securely salted and hashed password string in "salt$hash" format.
     */
    fun createSecurePasswordRecord(plain: String): String {
        val salt = generateSalt()
        val hash = hashPassword(plain, salt)
        return "$salt\$$hash"
    }

    /**
     * SHA-256 with salt fallback.
     */
    fun sha256WithSalt(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /**
     * Verifies an entered password against a stored hash or plain legacy password.
     */
    fun verifyPassword(entered: String, storedPasswordRecord: String): Boolean {
        if (storedPasswordRecord.isEmpty()) return false

        // 1. Direct match (plain text legacy / default password)
        if (entered == storedPasswordRecord) {
            return true
        }

        // 2. Salted format: "salt$hash"
        if (storedPasswordRecord.contains("$")) {
            val parts = storedPasswordRecord.split("$", limit = 2)
            if (parts.size == 2) {
                val salt = parts[0]
                val expectedHash = parts[1]
                val computedHash = hashPassword(entered, salt)
                if (computedHash == expectedHash) return true

                val computedSha = sha256WithSalt(entered, salt)
                if (computedSha == expectedHash) return true
            }
        }

        // 3. Fallback for empty default
        if (storedPasswordRecord.ifEmpty { "427752" } == entered) {
            return true
        }

        return false
    }

    /**
     * Check if an account is currently locked out due to excessive failed attempts.
     * Returns remaining lockout seconds (0 if not locked).
     */
    fun getRemainingLockoutSeconds(identifier: String): Long {
        val key = identifier.trim().lowercase()
        val record = failedAttemptsTracker[key] ?: return 0L
        val now = System.currentTimeMillis()
        val lockoutUntil = record.second

        if (now < lockoutUntil) {
            return ((lockoutUntil - now) / 1000L).coerceAtLeast(1L)
        } else if (lockoutUntil > 0L) {
            failedAttemptsTracker.remove(key)
        }
        return 0L
    }

    /**
     * Records a failed login attempt and returns pair of (attemptCount, remainingLockoutSeconds).
     */
    fun recordFailedAttempt(identifier: String): Pair<Int, Long> {
        val key = identifier.trim().lowercase()
        val current = failedAttemptsTracker[key]
        val now = System.currentTimeMillis()

        val newCount = (current?.first ?: 0) + 1
        val lockoutUntil = if (newCount >= MAX_FAILED_ATTEMPTS) {
            now + LOCKOUT_DURATION_MS
        } else {
            0L
        }

        val updated = Pair(newCount, lockoutUntil)
        failedAttemptsTracker[key] = updated

        val remainingSecs = if (lockoutUntil > now) ((lockoutUntil - now) / 1000L) else 0L
        return Pair(newCount, remainingSecs)
    }

    /**
     * Clears failed login attempts after a successful login.
     */
    fun clearFailedAttempts(identifier: String) {
        val key = identifier.trim().lowercase()
        failedAttemptsTracker.remove(key)
    }

    /**
     * Validates password strength according to corporate security policy.
     */
    fun validatePasswordStrength(password: String): PasswordValidationResult {
        val trimmed = password.trim()
        if (trimmed.length < 6) {
            return PasswordValidationResult(false, "Password must be at least 6 characters long.")
        }
        if (trimmed == "427752") {
            return PasswordValidationResult(false, "Default temporary password '427752' cannot be reused.")
        }
        val hasLetter = trimmed.any { it.isLetter() }
        val hasDigit = trimmed.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return PasswordValidationResult(false, "Password must contain both letters and numbers for partner security.")
        }
        return PasswordValidationResult(true, null)
    }

    /**
     * Generates a cryptographically secure session token.
     */
    fun generateSessionToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
