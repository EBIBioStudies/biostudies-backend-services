package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {

    @Bean
    fun lockExecutor() = object : LockExecutor {
        override fun executeLocking(lockName: String, timeout: Int, executable: () -> Unit) = executable()
    }
}
