package csw.scylladb.jwt.kotlintest.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*


object CookieUtil {
    fun getCookie(request: HttpServletRequest, name: String): Optional<Cookie> {
        val cookies = request.cookies
        return if (cookies == null) Optional.empty()
        else Arrays.stream(cookies)
            .filter { cookie: Cookie -> name == cookie.name }
            .findFirst()
    }

    fun addCookie(response: HttpServletResponse, name: String?, value: String?, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies ?: return

        for (cookie in cookies) {
            if (name == cookie.name) {
                addCookie(response, name, "/", 0)
            }
        }
    }

    // Production
    fun addSecureCookie(
        response: HttpServletResponse,
        name: String?,
        value: String?,
        maxAge: Int,
        isSecure: Boolean,
        sameSite: String?
    ) {
        val cookieValue = String.format(
            "%s=%s; Max-Age=%d; Path=/; HttpOnly%s%s",
            name, value, maxAge,
            if (isSecure) "; Secure" else "",
            if (sameSite != null) "; SameSite=$sameSite" else ""
        )
        response.addHeader("Set-Cookie", cookieValue)
    }
}

