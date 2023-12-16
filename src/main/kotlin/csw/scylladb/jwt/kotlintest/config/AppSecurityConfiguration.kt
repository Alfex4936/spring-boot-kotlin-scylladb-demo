package csw.scylladb.jwt.kotlintest.config

import com.fasterxml.jackson.databind.ObjectMapper
import csw.scylladb.jwt.kotlintest.config.JwtTokenFilter.Companion.ACCESS_TOKEN_COOKIE
import csw.scylladb.jwt.kotlintest.config.security.CustomAuthenticationEntryPoint
import csw.scylladb.jwt.kotlintest.dto.LoginResponse
import csw.scylladb.jwt.kotlintest.model.CustomOAuth2User
import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.service.CustomOAuth2UserService
import csw.scylladb.jwt.kotlintest.service.TokenService
import csw.scylladb.jwt.kotlintest.service.auth.UserDetailService
import csw.scylladb.jwt.kotlintest.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.io.IOException


@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class AppSecurityConfiguration(
    private val tokenService: TokenService,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val objectMapper: ObjectMapper,
    private val userDetailsService: UserDetailService
) {

    @Value("\${server.frontend}")
    private lateinit var clientUrl: String

    private val log: Logger = LoggerFactory.getLogger(AppSecurityConfiguration::class.java)


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
        configuration.setAllowedOriginPatterns(
            listOf(
                "http://*:8123",
                "http://*:8080"
            )
        ) // Explicitly set the allowed origin

//        configuration.allowedOrigins = listOf("*")
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
        http.sessionManagement { s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.exceptionHandling { c: ExceptionHandlingConfigurer<HttpSecurity?> ->
            c.authenticationEntryPoint(
                CustomAuthenticationEntryPoint()
            )
        }

        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(AntPathRequestMatcher("/error")).permitAll()
                .requestMatchers(AntPathRequestMatcher("/login")).permitAll()
                .requestMatchers(AntPathRequestMatcher("/auth/signup")).permitAll()
                .requestMatchers(AntPathRequestMatcher("/oauth2/**")).permitAll()
                .anyRequest().authenticated()
        }

        http.formLogin { f: FormLoginConfigurer<HttpSecurity?> ->
            f.loginPage("/auth/login").usernameParameter("email").passwordParameter("password").permitAll()
                .failureHandler { _: HttpServletRequest?, response: HttpServletResponse, _: AuthenticationException? ->
                    log.error("Handling failure, redirecting to login page.")
                    // Set the content type of the response to JSON
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    response.status = HttpStatus.FORBIDDEN.value()

                    // Create a JSON object with the error message
                    val jsonMessage = "{\"msg\":\"Unauthorized. Please provide correct user info.\"}"

                    // Write the JSON message to the response
                    try {
                        response.outputStream.write(jsonMessage.toByteArray())
                    } catch (e: IOException) {
                        throw java.lang.RuntimeException(e)
                    }
                }
                .successHandler((AuthenticationSuccessHandler { _: HttpServletRequest?, response: HttpServletResponse, authentication: Authentication ->
                    log.info("Login successful for user: {}", authentication.name)
                    val user = authentication.principal as User
                    val accessToken = tokenService.generateOpaqueToken()
                    tokenService.saveOpaqueToken(user.username, user.provider!!, accessToken)

                    log.info("ACCESS TOKEN: {}", accessToken)

                    response.contentType = "application/json"
                    response.characterEncoding = "utf-8"
                    val login = LoginResponse(accessToken, user)

                    val result = objectMapper.writeValueAsString(login)

                    response.addHeader("Authorization", "Bearer $accessToken")
                    response.writer.write(result)
                }))
        }

        http.oauth2Login { o: OAuth2LoginConfigurer<HttpSecurity?> ->
            o.successHandler((AuthenticationSuccessHandler { _: HttpServletRequest, response: HttpServletResponse, authentication: Authentication ->
                log.info("Login successful for user: {}", authentication)
                val user: User = (authentication.principal as CustomOAuth2User).user

                // Generate access token
                val accessToken: String = tokenService.generateOpaqueToken()
                tokenService.saveOpaqueToken(user.email, user.provider!!, accessToken)
                response.sendRedirect("$clientUrl/token?token=$accessToken")
            }))
                .failureHandler { _: HttpServletRequest, response: HttpServletResponse, _: AuthenticationException ->
                    log.error("Handling failure, redirecting to login page.")
                    // Set the content type of the response to JSON
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    response.status = HttpStatus.FORBIDDEN.value()

                    // Create a JSON object with the error message
                    val jsonMessage = "{\"msg\":\"Unauthorized. Please provide correct user info.\"}"

                    // Write the JSON message to the response
                    try {
                        response.outputStream.write(jsonMessage.toByteArray())
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
                .userInfoEndpoint { c ->
                    c.userService(
                        customOAuth2UserService
                    )
                }
        }

        http.logout { l: LogoutConfigurer<HttpSecurity?> ->
            l
                .logoutRequestMatcher(AntPathRequestMatcher.antMatcher("/logout"))
                .logoutSuccessHandler((LogoutSuccessHandler { request: HttpServletRequest, response: HttpServletResponse, _: Authentication ->
                    try {
                        CookieUtil.deleteCookie(request, response, ACCESS_TOKEN_COOKIE)
                    } catch (e: java.lang.Exception) {
                        log.error(e.message)
                    }
                }))
        }

        http.authenticationProvider(authenticationProvider())
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun tokenAuthenticationFilter(): JwtTokenFilter {
        return JwtTokenFilter(tokenService)
    }

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()

        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(bCryptPasswordEncoder())

        return authProvider
    }
}
