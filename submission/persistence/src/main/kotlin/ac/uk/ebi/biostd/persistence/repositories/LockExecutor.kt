package ac.uk.ebi.biostd.persistence.repositories

import org.apache.commons.lang3.ObjectUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

private const val NAME_PARAM = "LOCK_NAME"
private const val TIME_PARAM = "TIMEOUT"

private const val LOCK_QUERY = "SELECT GET_LOCK(:LOCK_NAME, :TIMEOUT)"
private const val RELEASE_QUERY = "SELECT RELEASE_LOCK(:LOCK_NAME)"

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

        throw LockException(lockName)
    }

    private fun releaseLock(lockName: String) {
        template.queryForObject<Int>(RELEASE_QUERY, mapOf(NAME_PARAM to lockName), Int::class.java)
    }

    private fun acquireLock(lockName: String, timeout: Int): Boolean {
        val params = mapOf(NAME_PARAM to lockName, TIME_PARAM to timeout)
        val lock = template.queryForObject<Int>(LOCK_QUERY, params, Int::class.java)
        return ObjectUtils.compare(lock, 1) == 0
    }
}

class LockException(lockName: String) : Exception("Lock $lockName can not be adquired")
