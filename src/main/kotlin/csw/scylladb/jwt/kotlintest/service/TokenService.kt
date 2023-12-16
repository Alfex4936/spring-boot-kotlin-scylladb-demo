package csw.scylladb.jwt.kotlintest.service

import csw.scylladb.jwt.kotlintest.model.OpaqueToken
import csw.scylladb.jwt.kotlintest.repository.OpaqueTokenRepository
import csw.scylladb.jwt.kotlintest.repository.UserRepository
import io.jsonwebtoken.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class TokenService(
    private val userRepository: UserRepository,
    private val opaqueTokenRepository: OpaqueTokenRepository
) {
    fun generateOpaqueToken(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        return base64Encoder.encodeToString(randomBytes)
    }

    fun saveOpaqueToken(username: String, provider: String, token: String) {
        val expirationDate = Instant.now().plus(OPAQUE_TOKEN_VALIDITY)

        val userToken = opaqueTokenRepository.findByProviderAndUsername(provider, username)
            ?.apply {
                // If token exists, update it
                this.opaqueToken = token
                this.expiresAt = expirationDate
            } ?: OpaqueToken(
            // If token does not exist, create a new one
            username = username,
            provider = provider,
            opaqueToken = token,
            expiresAt = expirationDate
        )

        opaqueTokenRepository.save(userToken)
    }


    fun isOpaqueTokenValid(token: String): Boolean {
        return opaqueTokenRepository.findByToken(token)
            ?.let {
                it.expiresAt?.isAfter(Instant.now()) ?: false
            } ?: false
    }

    @Throws(UsernameNotFoundException::class)
    fun getAuthentication(token: String): Authentication {
        val userToken: OpaqueToken = opaqueTokenRepository.findByToken(token)
            ?: throw UsernameNotFoundException("Token not found or expired")

        val provider = userToken.provider
        val user = userRepository.findByProviderAndEmail(provider, userToken.username)
            ?: throw UsernameNotFoundException("User not found with username: " + userToken.username)

        return OAuth2AuthenticationToken(user, user.getAuthorities(), provider)
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    fun cleanUpExpiredTokens() {
        opaqueTokenRepository.deleteAllByExpiresAtBefore(Instant.now())
    }

    companion object {
//        private val OPAQUE_TOKEN_VALIDITY: Duration = Duration.ofDays(1)
        private val OPAQUE_TOKEN_VALIDITY: Duration = Duration.ofSeconds(60)
        private val secureRandom = SecureRandom() //thread-safe
        private val base64Encoder: Base64.Encoder = Base64.getUrlEncoder() //thread-safe
    }
}
