package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.lock.DistributedLockExecutor

class DistributedLockService internal constructor(
    private val distributedLockExecutor: DistributedLockExecutor,
) {
    suspend fun <T> onLockRequest(accNo: String, version: Int, lockOwner: String, handler: suspend () -> T): T {
        val lockId = "REQUEST_${accNo}_$version"
        try {
            val locked = distributedLockExecutor.acquireLock(lockId, lockOwner)
            return when {
                locked -> handler()
                else -> throw IllegalStateException("Could not lock submission, accNo='$accNo', version='$version'")
            }
        } finally {
            distributedLockExecutor.releaseLock(lockId, lockOwner)
        }
    }
}
