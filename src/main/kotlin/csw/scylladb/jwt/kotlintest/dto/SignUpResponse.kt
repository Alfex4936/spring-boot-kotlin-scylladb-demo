package csw.scylladb.jwt.kotlintest.dto

import csw.scylladb.jwt.kotlintest.model.User


data class SignUpResponse(
    val user: User,
)