package csw.scylladb.jwt.kotlintest.service

import csw.scylladb.jwt.kotlintest.dto.UserCreationDto
import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val userRepository: UserRepository, private val bCryptPasswordEncoder: BCryptPasswordEncoder) {

    fun saveUser(userDto: UserCreationDto): User {
        // Check if a user with the same email already exists
        userRepository.findByProviderAndEmail("website", userDto.email)?.let {
            throw DataIntegrityViolationException("A user with email ${userDto.email} already exists.")
        }

        val user = User(
            email = userDto.email,
            userPassword = bCryptPasswordEncoder.encode(userDto.password),
            userName = userDto.nickname,
            picture = userDto.profileImage,
            phoneNumber = userDto.phoneNumber,
            provider = "website",
            permissions = setOf("ROLE_USER")
            )

        user.markAsNew()

        return userRepository.save(user)
    }

    fun getAll(): List<User> {
        return userRepository.findAll()
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: UUID): Optional<User> {
        print("AM i calling???")
        return userRepository.findById(id)
    }
}
