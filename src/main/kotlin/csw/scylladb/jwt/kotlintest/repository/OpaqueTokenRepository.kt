package csw.scylladb.jwt.kotlintest.repository

import csw.scylladb.jwt.kotlintest.model.OpaqueToken
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
interface OpaqueTokenRepository : CassandraRepository<OpaqueToken, UUID> {

    @Query("UPDATE opaque_tokens SET opaque_token = :opaqueToken, updated_at = :updatedAt, expires_at = :expiresAt USING TTL :ttl WHERE id = :id")
    fun updateToken(
        @Param("id") id: UUID,
        @Param("opaqueToken") opaqueToken: String,
        @Param("updatedAt") updatedAt: Instant,
        @Param("expiresAt") expiresAt: Instant,
        @Param("ttl") ttl: Int
    )

    @Query("INSERT INTO opaque_tokens (id, username, provider, opaque_token, created_at, updated_at, expires_at) VALUES (:id, :username, :provider, :opaqueToken, :createdAt, :updatedAt, :expiresAt) USING TTL :ttl")
    fun insertToken(
        @Param("id") id: UUID,
        @Param("username") username: String,
        @Param("provider") provider: String,
        @Param("opaqueToken") opaqueToken: String,
        @Param("createdAt") createdAt: Instant,
        @Param("updatedAt") updatedAt: Instant,
        @Param("expiresAt") expiresAt: Instant,
        @Param("ttl") ttl: Int
    )

    @Query("SELECT * FROM opaque_tokens WHERE opaque_token = :token ALLOW FILTERING")
    fun findByToken(@Param("token") token: String): OpaqueToken?

    @Query("SELECT * FROM opaque_tokens WHERE provider = :provider AND username = :username ALLOW FILTERING")
    fun findByProviderAndUsername(
        @Param("provider") provider: String,
        @Param("username") username: String
    ): OpaqueToken?

    @Query("SELECT * FROM opaque_tokens WHERE expires_at < ? ALLOW FILTERING")
    fun findAllByExpiresAtBefore(@Param("dateTime") dateTime: Instant): List<OpaqueToken>

    @Transactional
    fun deleteAllByExpiresAtBefore(dateTime: Instant) {
        val tokensToDelete = findAllByExpiresAtBefore(dateTime)
        tokensToDelete.forEach { delete(it) }
    }

}