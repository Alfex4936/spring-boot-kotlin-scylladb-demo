package csw.scylladb.jwt.kotlintest.config.cassandra


import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator
import java.net.InetSocketAddress


@Configuration
@Profile("!unit-test & !integration-test")
class ScyllaConfiguration {
    @Value("\${scylla.contactPoints}")
    private lateinit var rawContactPoints: String

    private val contactPoints: List<String>
        get() = rawContactPoints.split(",").filter { it.isNotBlank() }

    @Value("\${scylla.port:9042}")
    private val port: Int = 0

    @Value("\${scylla.localdc}")
    private val localDc: String? = null

    @Value("\${scylla.keyspace}")
    private val keyspaceName: String? = null

    @Value("\${scylla.consistency:LOCAL_QUORUM}")
    private val consistency: String = "LOCAL_QUORUM"

    @Value("\${scylla.username}")
    private val username: String? = null

    @Value("\${scylla.password}")
    private val password: String? = null

    @Value("\${scylla.replicationFactor:2}")
    private val replicationFactor: Int = 2 // two nodes

    @Bean
    fun keyspacePopulator() = ResourceKeyspacePopulator().apply {
        setScripts(ClassPathResource("init.cql"))
    }

    @Bean
    fun getSchemaAction() = SchemaAction.CREATE_IF_NOT_EXISTS


    @Bean
    fun configLoaderBuilder() = DriverConfigLoader.programmaticBuilder().apply {
        withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency)
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            withString(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider::class.java.name)
            withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, username)
            withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, password)
        }
    }

    @Bean
    fun sessionBuilder(driverConfigLoaderBuilder: ProgrammaticDriverConfigLoaderBuilder) = CqlSessionBuilder().apply {
        withConfigLoader(driverConfigLoaderBuilder.build())
        contactPoints.forEach {
            addContactPoint(InetSocketAddress.createUnresolved(it, port))
        }
        withLocalDatacenter(localDc ?: throw IllegalStateException("Local DC must be specified"))
    }

    // create keyspace in init.cql
    @Bean
    fun session(sessionBuilder: CqlSessionBuilder, keyspacePopulator: KeyspacePopulator): CqlSession {
        // Build and use a temporary session to populate the keyspace
        sessionBuilder.build().use { tempSession ->
            keyspacePopulator.populate(tempSession)
        }

        // Rebuild and return the session with the keyspace
        return sessionBuilder.withKeyspace(keyspaceName).build()
    }


    // creating keyspace in java
//    @Bean
//    fun session(sessionBuilder: CqlSessionBuilder, keyspacePopulator: KeyspacePopulator): CqlSession =
//        sessionBuilder.build().apply {
//            keyspaceName?.let { createKeyspaceIfNeeded(this, it) }
//            close()
//        }.let {
//            sessionBuilder.withKeyspace(keyspaceName).build().apply {
//                keyspacePopulator.populate(this)
//            }
//        }
    private fun createKeyspaceIfNeeded(session: CqlSession, keyspaceName: String) {
        val query =
            "CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH replication = {'class': 'NetworkTopologyStrategy', 'replication_factor' : $replicationFactor};"
        session.execute(query)
    }
}