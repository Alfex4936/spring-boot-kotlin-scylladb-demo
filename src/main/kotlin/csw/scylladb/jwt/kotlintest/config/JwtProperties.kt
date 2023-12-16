package csw.scylladb.jwt.kotlintest.config

import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Setter
@Getter
@Component
@ConfigurationProperties("jwt")
class JwtProperties {
    private val issuer: String? = null
    private val secretKey: String? = null
}

