package csw.scylladb.jwt.kotlintest.dto

import csw.scylladb.jwt.kotlintest.model.User


data class LoginResponse(
    val accessToken: String,
    val user: User,
)