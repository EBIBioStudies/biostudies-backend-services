package ac.uk.ebi.biostd.persistence.repositories

import mu.KotlinLogging
import org.apache.commons.lang3.ObjectUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

private const val NAME_PARAM = "LOCK_NAME"
private const val TIME_PARAM = "TIMEOUT"

private const val LOCK_QUERY = "SELECT GET_LOCK(:LOCK_NAME, :TIMEOUT)"
private const val RELEASE_QUERY = "SELECT RELEASE_LOCK(:LOCK_NAME)"

private val logger = KotlinLogging.logger {}

interface LockExecutor {
    fun <T> executeLocking(lockName: String, timeout: Int = 3, executable: () -> T): T
}

class JdbcLockExecutor(private val template: NamedParameterJdbcTemplate) : LockExecutor {

    override fun <T> executeLocking(lockName: String, timeout: Int, executable: () -> T): T {
        if (acquireLock(lockName, timeout)) {
            try {
                return executable()
            } finally {
                releaseLock(lockName)
            }
        }

        throw IllegalStateException("could not acquire lock $lockName")
    }

    private fun releaseLock(lockName: String) {
        logger.info { "releasing lock $lockName in ${Thread.currentThread()}" }
        template.queryForObject<Int>(RELEASE_QUERY, mapOf(NAME_PARAM to lockName), Int::class.java)
    }

    private fun acquireLock(lockName: String, timeout: Int): Boolean {
        logger.info { "acquiring lock $lockName in ${Thread.currentThread()}" }
        val params = mapOf(NAME_PARAM to lockName, TIME_PARAM to timeout)
        val lock = template.queryForObject<Int>(LOCK_QUERY, params, Int::class.java)
        return ObjectUtils.compare(lock, 1) == 0
    }
}
