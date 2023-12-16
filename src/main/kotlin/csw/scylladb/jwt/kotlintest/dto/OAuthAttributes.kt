package csw.scylladb.jwt.kotlintest.dto

import csw.scylladb.jwt.kotlintest.model.User
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.util.*
import kotlin.collections.HashMap

data class OAuthAttributes(
    val attributes: Map<String, Any>,
    val nameAttributeKey: String,
    val name: String,
    val provider: String,
    val email: String,
    val picture: String
) {
    companion object {
        private val log = LoggerFactory.getLogger(OAuthAttributes::class.java)

        fun of(registrationId: String, userNameAttributeName: String, attributes: Map<String, Any>, env: Environment): OAuthAttributes {
            log.info("OF: {}", attributes)

            val providerPrefix = registrationId.lowercase(Locale.getDefault()) + "."
            val name = attributes.getValueWithKey(env.getProperty(providerPrefix + "name"))
            val email = attributes.getValueWithKey(env.getProperty(providerPrefix + "email"))
            val picture = attributes.getValueWithKey(env.getProperty(providerPrefix + "picture"))

            return OAuthAttributes(
                provider = registrationId,
                name = name ?: "",
                email = email ?: "",
                picture = picture ?: "",
                attributes = HashMap(attributes),
                nameAttributeKey = userNameAttributeName
            )
        }

        private fun Map<String, Any>.getValueWithKey(path: String?): String? {
            if (path.isNullOrEmpty()) return null

            val pathComponents = path.split(".")
            var currentMap: Map<String, Any>? = this

            for (component in pathComponents.dropLast(1)) {
                currentMap = currentMap?.get(component) as? Map<String, Any> ?: return null
            }

            return currentMap?.get(pathComponents.last()) as? String
        }

    }

    fun toEntity(): User {
        val user = User(
            userName = name,
            provider = provider,
            email = email,
            picture = picture,
            permissions = setOf("ROLE_USER"),
        )
        user.markAsNew()
        return user
    }
}
