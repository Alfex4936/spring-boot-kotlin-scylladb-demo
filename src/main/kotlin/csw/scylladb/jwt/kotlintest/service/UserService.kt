package csw.scylladb.jwt.kotlintest.service

import csw.scylladb.jwt.kotlintest.dto.UserCreationDto
import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(private val userRepository: UserRepository) {

    fun saveUser(userDto: UserCreationDto): User {
        // Check if a user with the same email already exists
        userRepository.findByEmail(userDto.email)?.let {
            throw DataIntegrityViolationException("A user with email ${userDto.email} already exists.")
        }

        val user = User(
            email = userDto.email,
            password = userDto.password,
            nickname = userDto.nickname,
            profileImage = userDto.profileImage,
            phoneNumber = userDto.phoneNumber,

            )

        user.authorities = setOf("ROLE_USER")
        return userRepository.save(user)
    }

    fun getAll(): List<User> {
        return userRepository.findAll()
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
}
