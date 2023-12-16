package csw.scylladb.jwt.kotlintest.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:oauth2-attributes.properties")
class OAuthEnv