package csw.scylladb.jwt.kotlintest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.cassandra.config.EnableCassandraAuditing

@SpringBootApplication
@EnableCassandraAuditing
class KotlinTestApplication

fun main(args: Array<String>) {
	runApplication<KotlinTestApplication>(*args)
}
