package csw.scylladb.jwt.kotlintest.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException

class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        // Create a JSON object with the error message
        val jsonMessage = "{\"msg\":\"Unauthorized. Please provide correct user info.\"}"
        try {
            response.outputStream.write(jsonMessage.toByteArray())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
