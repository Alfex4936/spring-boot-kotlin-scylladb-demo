package csw.scylladb.jwt.kotlintest.controller

import csw.scylladb.jwt.kotlintest.dto.UserCreationDto
import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController @Autowired constructor(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody userDto: UserCreationDto): ResponseEntity<User> {
        val savedUser = userService.saveUser(userDto)
        return ResponseEntity.ok(savedUser)
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.ok(userService.getAll())
    }

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        val user = userService.getUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
