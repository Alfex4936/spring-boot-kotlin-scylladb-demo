package csw.scylladb.jwt.kotlintest.service.auth

import csw.scylladb.jwt.kotlintest.model.User
import csw.scylladb.jwt.kotlintest.repository.UserRepository
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Slf4j
@RequiredArgsConstructor
@Service
class UserDetailService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found with email: $email")
    }
}
