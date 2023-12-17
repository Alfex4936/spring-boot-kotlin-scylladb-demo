package csw.scylladb.jwt.kotlintest.model

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("opaque_tokens")
data class OpaqueToken(
    @field:PrimaryKey val id: UUID = Uuids.timeBased(),

    val username: String,
    val provider: String,

    @field:Column("opaque_token") var opaqueToken: String,

    @field:Column("created_at") @CreatedDate val createdAt: Instant? = null,

    @field:Column("updated_at") @LastModifiedDate val updatedAt: Instant? = null,

    @field:Column("expires_at") var expiresAt: Instant? = null,

    )
