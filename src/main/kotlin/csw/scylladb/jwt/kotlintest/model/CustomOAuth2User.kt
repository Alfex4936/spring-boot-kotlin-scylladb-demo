package csw.scylladb.jwt.kotlintest.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

data class CustomOAuth2User(
    val user: User,
    val authAttributes: Map<String, Any>  // Renamed to avoid clash
) : OAuth2User {

    override fun getAttributes(): Map<String, Any> = authAttributes

    override fun getAuthorities(): Collection<GrantedAuthority> = user.getAuthorities()

    override fun getName(): String = user.username
}
