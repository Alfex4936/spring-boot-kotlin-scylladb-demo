package csw.scylladb.jwt.kotlintest.controller

import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.service.TokenService
import csw.scylladb.jwt.kotlintest.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ROLE_USER')")
class UserController @Autowired constructor(
    private val userService: UserService,
    private val tokenService: TokenService
) {
    private val log: Logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping("/profile")
    fun getUserProfile(@AuthenticationPrincipal user: User): ResponseEntity<User> {
        return userService.findById(user.id).map {
            ResponseEntity.ok(it)
        }.orElseGet {
            ResponseEntity.badRequest().build()
        }
    }
}
