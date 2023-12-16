package csw.scylladb.jwt.kotlintest.repository

import csw.scylladb.jwt.kotlintest.model.OpaqueToken
import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
interface OpaqueTokenRepository : CassandraRepository<OpaqueToken, UUID> {

    @Query("SELECT * FROM opaque_tokens WHERE opaque_token = :token ALLOW FILTERING")
    fun findByToken(@Param("token") token: String): OpaqueToken?

    @Query("SELECT * FROM opaque_tokens WHERE provider = :provider AND username = :username ALLOW FILTERING")
    fun findByProviderAndUsername(
        @Param("provider") provider: String,
        @Param("username") username: String
    ): OpaqueToken?

    @Query("SELECT * FROM opaque_tokens WHERE expires_at < ? ALLOW FILTERING")
    fun findAllByExpiresAtBefore(dateTime: Instant): List<OpaqueToken>

    @Transactional
    fun deleteAllByExpiresAtBefore(dateTime: Instant) {
        val tokensToDelete = findAllByExpiresAtBefore(dateTime)
        tokensToDelete.forEach { delete(it) }
    }

}