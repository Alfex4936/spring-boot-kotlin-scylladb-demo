package csw.scylladb.jwt.kotlintest.dto


data class SignUpRequest(
    val email: String,
    val password: String
)