package csw.scylladb.jwt.kotlintest.model

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("users")
data class User(
    @field:PrimaryKey val id: UUID = Uuids.timeBased(),

    val email: String,
    val password: String,

    var nickname: String? = null,

    @field:Column("profile_image")
    var profileImage: String? = null,

    @field:Column("phone_number")
    var phoneNumber: String? = null,

    @field:Column("created_at")
    @CreatedDate val createdAt: Instant? = null,

    @field:Column("updated_at")
    @LastModifiedDate val updatedAt: Instant? = null,

    @field:Column("last_login")
    var lastLogin: Instant? = null,

    @field:Column("authorities")
    private var _authorities: String? = null
) {
    var authorities: Set<String>
        get() = _authorities?.split(",")?.toSet() ?: setOf()
        set(value) {
            _authorities = value.joinToString(",")
        }
}