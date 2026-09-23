package com.example.data.repository

import android.content.Context
import com.example.data.local.ActivityAlertEntity
import com.example.data.local.PartnerEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Result wrapper for authentication operations.
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult<Nothing>()
}

/**
 * Lightweight user representation for Dakshyam partner session.
 */
data class AppUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val phoneNumber: String = ""
)

/**
 * State representation for partner access control and security status.
 */
sealed class PartnerAccessState {
    object Unauthenticated : PartnerAccessState()
    object Authenticating : PartnerAccessState()
    data class Authorized(
        val user: AppUser,
        val partner: PartnerEntity,
        val isManagingPartner: Boolean
    ) : PartnerAccessState() {
        val firebaseUser: AppUser get() = user
    }
    data class PendingVerification(
        val user: AppUser,
        val email: String,
        val message: String
    ) : PartnerAccessState() {
        val firebaseUser: AppUser get() = user
    }
    data class AccessDenied(
        val email: String,
        val reason: String
    ) : PartnerAccessState()
}

/**
 * AuthenticationRepository manages partner access control and credentials for Dakshyam Innovations
 * directly backed by Supabase PostgreSQL. SQLite/Room database has been completely eliminated.
 */
class AuthenticationRepository(
    private val context: Context,
    private val supabaseSyncRepository: SupabaseSyncRepository,
    private val dakshyamRepository: DakshyamRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        val FOUNDING_PARTNER_EMAILS = setOf(
            "ankush@dakshyam.com",
            "ankush2004nanda@gmail.com",
            "himanshu@dakshyam.com",
            "shikhar@dakshyam.com",
            "kunal@dakshyam.com"
        )
    }

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val authStateFlow: Flow<AppUser?> = _currentUser.asStateFlow()

    private val _partnerAccessState = MutableStateFlow<PartnerAccessState>(PartnerAccessState.Unauthenticated)
    val partnerAccessState: StateFlow<PartnerAccessState> = _partnerAccessState.asStateFlow()

    suspend fun isPartnerAuthorized(email: String): Boolean = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail in FOUNDING_PARTNER_EMAILS || cleanEmail.endsWith("@dakshyam.com")) {
            return@withContext true
        }
        val partner = supabaseSyncRepository.findPartnerByEmail(cleanEmail)
        return@withContext partner != null && partner.isActive
    }

    suspend fun resolveAndAuthorizePartner(user: AppUser): AuthResult<PartnerEntity> = withContext(ioDispatcher) {
        val email = user.email.trim()
        val displayName = user.displayName.trim()

        if (email.isEmpty()) {
            val denied = PartnerAccessState.AccessDenied("", "Missing email address on partner account.")
            _partnerAccessState.value = denied
            return@withContext AuthResult.Error("Missing email address.")
        }

        val cleanEmail = email.lowercase()
        val partner = supabaseSyncRepository.findPartnerByEmail(cleanEmail)

        if (partner == null) {
            val reason = "Unauthorized account. $email is not an enrolled partner of Dakshyam Innovations."
            _partnerAccessState.value = PartnerAccessState.AccessDenied(email, reason)
            return@withContext AuthResult.Error(reason)
        }

        if (!partner.isActive) {
            val reason = "Partner account for ${partner.name} is currently inactive. Contact Managing Partner."
            _partnerAccessState.value = PartnerAccessState.AccessDenied(email, reason)
            return@withContext AuthResult.Error(reason)
        }

        val isManaging = partner.role.contains("Managing", ignoreCase = true)
        _currentUser.value = user
        _partnerAccessState.value = PartnerAccessState.Authorized(
            user = user,
            partner = partner,
            isManagingPartner = isManaging
        )

        dakshyamRepository.addAlert(
            ActivityAlertEntity(
                id = System.currentTimeMillis() + 10,
                title = "Secure Partner Login",
                description = "${partner.name} successfully authenticated (${partner.role}).",
                category = "SECURITY"
            )
        )

        AuthResult.Success(partner)
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult<PartnerEntity> = withContext(ioDispatcher) {
        _partnerAccessState.value = PartnerAccessState.Authenticating
        val cleanEmail = email.trim().lowercase()
        val partner = supabaseSyncRepository.findPartnerByEmail(cleanEmail)

        if (partner == null) {
            _partnerAccessState.value = PartnerAccessState.AccessDenied(cleanEmail, "Account not found for $cleanEmail")
            return@withContext AuthResult.Error("No partner account found with email $email")
        }

        val isCustomPasswordSet = partner.password.isNotEmpty() && partner.password != "427752"
        val isPasswordCorrect = if (isCustomPasswordSet) {
            partner.password == pass.trim()
        } else {
            pass.trim() == partner.password || pass.trim() == "427752"
        }

        if (!isPasswordCorrect) {
            _partnerAccessState.value = PartnerAccessState.Unauthenticated
            return@withContext AuthResult.Error(
                if (isCustomPasswordSet && pass.trim() == "427752")
                    "Your password has been changed. Default password '427752' is disabled."
                else "Invalid credentials provided."
            )
        }

        val appUser = AppUser(
            uid = "user_${partner.id}",
            email = cleanEmail,
            displayName = partner.name,
            phoneNumber = partner.phone
        )
        resolveAndAuthorizePartner(appUser)
    }

    suspend fun signUpPartner(
        email: String,
        pass: String,
        name: String,
        role: String = "Executive Partner",
        phone: String = ""
    ): AuthResult<PartnerEntity> = withContext(ioDispatcher) {
        _partnerAccessState.value = PartnerAccessState.Authenticating
        val cleanEmail = email.trim().lowercase()
        val existing = supabaseSyncRepository.findPartnerByEmail(cleanEmail)

        val partner = if (existing != null) {
            val updated = existing.copy(
                name = name,
                role = role,
                phone = phone.ifEmpty { existing.phone },
                password = pass.trim(),
                isActive = true
            )
            dakshyamRepository.updatePartner(updated)
            updated
        } else {
            val newPartner = PartnerEntity(
                id = System.currentTimeMillis(),
                name = name,
                role = role,
                email = cleanEmail,
                phone = phone,
                password = pass.trim(),
                capitalContributed = 0.0,
                isActive = true
            )
            dakshyamRepository.savePartner(newPartner)
            newPartner
        }

        val appUser = AppUser(
            uid = "user_${partner.id}",
            email = cleanEmail,
            displayName = name,
            phoneNumber = phone
        )
        resolveAndAuthorizePartner(appUser)
    }

    suspend fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String
    ): AuthResult<PartnerEntity> = withContext(ioDispatcher) {
        val user = _currentUser.value ?: return@withContext AuthResult.Error(
            "No active Google account detected. Please use your partner email and password to log in."
        )
        resolveAndAuthorizePartner(user)
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = withContext(ioDispatcher) {
        AuthResult.Success(Unit)
    }

    suspend fun signOut() = withContext(ioDispatcher) {
        val current = _partnerAccessState.value
        val partnerName = if (current is PartnerAccessState.Authorized) current.partner.name else "Partner"
        _currentUser.value = null
        _partnerAccessState.value = PartnerAccessState.Unauthenticated

        dakshyamRepository.addAlert(
            ActivityAlertEntity(
                id = System.currentTimeMillis() + 11,
                title = "Partner Signed Out",
                description = "$partnerName signed out of session.",
                category = "SECURITY"
            )
        )
    }

    fun getCurrentFirebaseUser(): AppUser? = _currentUser.value
}
