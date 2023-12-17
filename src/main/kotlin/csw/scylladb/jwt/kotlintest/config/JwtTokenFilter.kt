package csw.scylladb.jwt.kotlintest.config

import csw.scylladb.jwt.kotlintest.service.TokenService
import csw.scylladb.jwt.kotlintest.util.CookieUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.time.Duration
import java.util.regex.Pattern

@Slf4j
class JwtTokenFilter(private val tokenService: TokenService) : OncePerRequestFilter() {

    private val log: Logger = LoggerFactory.getLogger(JwtTokenFilter::class.java)

    companion object {
        val REFRESH_TOKEN_DURATION = Duration.ofDays(14)
        val SHORT_ACCESS_TOKEN_DURATION = Duration.ofMinutes(5)
        val LONG_ACCESS_TOKEN_DURATION = Duration.ofDays(1)

        const val HEADER_AUTHORIZATION = "Authorization"
        const val NEW_HEADER_AUTHORIZATION = "New-Access-Token"
        const val NEW_XRT_AUTHORIZATION = "New-Refresh-Token"
        const val ACCESS_TOKEN_COOKIE = "Access-Token"
        const val TOKEN_PREFIX = "Bearer "

        private val UNPROTECTED_API = Pattern.compile("^/(login|auth/signup|auth/login)(/.*)?$")
        private val PROTECTED_API = Pattern.compile("^/(logout|users)(/.*)?$")
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        if (isPublicRoute(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = getAccessToken(request)
        if (token != null && tokenService.isOpaqueTokenValid(token)) {
            try {
                val auth = tokenService.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: UsernameNotFoundException) {
                handleAuthenticationException(request, response, e)
                return
            }
        } else {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("{\"error\": \"Invalid or expired token\"}")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun isPublicRoute(uri: String): Boolean = UNPROTECTED_API.matcher(uri).matches()

    private fun isProtectedRoute(uri: String): Boolean = PROTECTED_API.matcher(uri).matches()

    private fun getAccessToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(HEADER_AUTHORIZATION)
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length)
        }

        return request.cookies?.find { it.name == ACCESS_TOKEN_COOKIE }?.value
    }

    @Throws(IOException::class)
    private fun handleAuthenticationException(
        request: HttpServletRequest, response: HttpServletResponse, e: UsernameNotFoundException
    ) {
        CookieUtil.deleteCookie(request, response, ACCESS_TOKEN_COOKIE)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: " + e.localizedMessage)
    }
}
