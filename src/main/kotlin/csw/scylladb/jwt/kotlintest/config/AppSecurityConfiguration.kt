package csw.scylladb.jwt.kotlintest.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class AppSecurityConfiguration {

    @Value("\${server.frontend}")
    private lateinit var clientUrl: String

    @Bean
    fun httpFirewall(): StrictHttpFirewall {
        val firewall = StrictHttpFirewall()
        firewall.setAllowSemicolon(true)
        firewall.setAllowBackSlash(true)
        firewall.setAllowUrlEncodedDoubleSlash(true)
        return firewall
    }

    @Bean
    fun configure(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(AntPathRequestMatcher("/img/**"))
                .requestMatchers(AntPathRequestMatcher("/css/**")).requestMatchers(AntPathRequestMatcher("/js/**"))
                .requestMatchers(AntPathRequestMatcher("/static/**"))
                // swagger
                .requestMatchers(AntPathRequestMatcher("/v3/api-docs/**"))
                .requestMatchers(AntPathRequestMatcher("/proxy/**"))
                .requestMatchers(AntPathRequestMatcher("/swagger-ui/**"))
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
//        configuration.allowedOriginPatterns = listOf(clientUrl)
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
        http.anonymous { obj: AnonymousConfigurer<HttpSecurity> -> obj.disable() }
        http.cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .sessionManagement { s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(AntPathRequestMatcher("/")).permitAll()
                    .requestMatchers(AntPathRequestMatcher("/error")).permitAll()
                    .requestMatchers(AntPathRequestMatcher("/login")).permitAll()
                    .requestMatchers(AntPathRequestMatcher("/auth/signup")).permitAll()
                    .requestMatchers(
                        AntPathRequestMatcher("/api/users/**")
                    ).permitAll()
                    .anyRequest().authenticated()
            }
        return http.build()
    }
}
