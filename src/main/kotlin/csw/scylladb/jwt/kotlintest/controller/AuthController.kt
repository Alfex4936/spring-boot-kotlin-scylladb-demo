package csw.scylladb.jwt.kotlintest.controller

import csw.scylladb.jwt.kotlintest.dto.SignUpRequest
import csw.scylladb.jwt.kotlintest.dto.SignUpResponse
import csw.scylladb.jwt.kotlintest.dto.UserCreationDto
import csw.scylladb.jwt.kotlintest.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signUp(@RequestBody request: UserCreationDto): ResponseEntity<SignUpResponse> {
        val user = userService.saveUser(request)

        val response = SignUpResponse(user)

        return ResponseEntity.ok(response)
    }
}
