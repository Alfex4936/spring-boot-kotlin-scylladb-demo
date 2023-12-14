package csw.scylladb.jwt.kotlintest.dto

data class UserCreationDto(
    val email: String,
    val password: String,
    var nickname: String? = null,
    var profileImage: String? = null,
    var phoneNumber: String? = null
)