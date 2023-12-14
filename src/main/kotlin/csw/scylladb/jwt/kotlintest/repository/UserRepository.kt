package csw.scylladb.jwt.kotlintest.repository

import csw.scylladb.jwt.kotlintest.model.User
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CassandraRepository<User, UUID> {
    @Query("SELECT * FROM users WHERE email = :email ALLOW FILTERING")
    fun findByEmail(@Param("email") email: String): User?
}