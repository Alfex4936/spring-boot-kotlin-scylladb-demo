package csw.scylladb.jwt.kotlintest.model

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.domain.Persistable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.Instant
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("users")
data class User(
    @field:PrimaryKey(value = "id") val userId: UUID = Uuids.timeBased(),

    val email: String,

    @field:Column("password") private val userPassword: String? = null, // Renamed to avoid clash with getPassword()

    var provider: String? = null,

    @field:Column("name") private var userName: String? = null, // Renamed to avoid clash with getName()

    var picture: String? = null, @field:Column("phone_number") var phoneNumber: String? = null,

    @field:Column("created_at") @CreatedDate val createdAt: Instant? = null,

    @JsonIgnore @field:Column("updated_at") @LastModifiedDate val updatedAt: Instant? = null,

    @field:Column("last_login") var lastLogin: Instant? = null,

    @JsonIgnore val permissions: Set<String> = mutableSetOf(),

    ) : UserDetails, OAuth2User, Persistable<UUID> {

    @org.springframework.data.annotation.Transient
    @JsonIgnore
    private var _isNew: Boolean = false // Backing field

    fun update(name: String?, picture: String?): User {
        this.userName = name
        this.picture = picture
        this._isNew = false
        return this
    }

    fun markAsNew() {
        _isNew = true
    }

    // Implement UserDetails and OAuth2User interface methods
    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> =
        permissions.map { SimpleGrantedAuthority(it) }.toList()

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true

    @JsonIgnore
    override fun isEnabled(): Boolean = true

    @JsonIgnore
    override fun getPassword(): String? = userPassword

    @JsonIgnore
    override fun getUsername(): String = email
    override fun getName(): String = email

    @JsonIgnore
    override fun getAttributes(): Map<String, Any>? = null

    @JsonIgnore
    override fun getId(): UUID = userId

    @JsonIgnore
    override fun isNew(): Boolean = _isNew
}