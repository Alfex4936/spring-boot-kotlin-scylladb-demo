package csw.scylladb.jwt.kotlintest

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.cassandra.config.EnableCassandraAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*


@EnableScheduling
@SpringBootApplication
@EnableCassandraAuditing
class KotlinTestApplication {

//    @PostConstruct
//    fun setTimeZone() {
//        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
//    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
            runApplication<KotlinTestApplication>(*args)
        }
    }
}