package csw.scylladb.jwt.kotlintest.controller

import csw.scylladb.jwt.kotlintest.dto.SignUpResponse
import csw.scylladb.jwt.kotlintest.dto.UserCreationDto
import csw.scylladb.jwt.kotlintest.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
