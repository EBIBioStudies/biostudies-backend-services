package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.lock.DistributedLockExecutor
import ebi.ac.uk.model.SubmissionId
import java.time.Duration

class DistributedLockService internal constructor(
    private val distributedLockExecutor: DistributedLockExecutor,
) {
    suspend fun <T> onLockRequest(
        accNo: String,
        version: Int,
        lockOwner: String,
        handler: suspend () -> T,
    ): T {
        val lockId = requestLockId(accNo, version)
        try {
            val locked = distributedLockExecutor.acquireLock(lockId, lockOwner, DEFAULT_LOCK_TIME)
            return when {
                locked -> handler()
                else -> error("Could not lock submission, accNo='$accNo', version='$version'")
            }
        } finally {
            distributedLockExecutor.releaseLock(lockId, lockOwner)
        }
    }

    suspend fun <T> onLockProcess(
        lockIdentifier: String,
        lockOwner: String,
        handler: suspend () -> T,
    ): T {
        try {
            val locked = distributedLockExecutor.acquireLock(lockIdentifier, lockOwner, DEFAULT_LOCK_TIME)
            return when {
                locked -> handler()
                else -> error("Could not acquire lock '$lockIdentifier'")
            }
        } finally {
            distributedLockExecutor.releaseLock(lockIdentifier, lockOwner)
        }
    }

    suspend fun getActiveSubmissionLocks(submissionIds: Collection<SubmissionId>): Set<SubmissionId> {
        val submissionsByLockId = submissionIds.associateBy { requestLockId(it.accNo, it.version) }
        return distributedLockExecutor
            .findActiveLocks(submissionsByLockId.keys)
            .mapTo(mutableSetOf()) { submissionsByLockId.getValue(it) }
    }

    private companion object {
        val DEFAULT_LOCK_TIME: Duration = Duration.ofDays(7)

        fun requestLockId(
            accNo: String,
            version: Int,
        ) = "REQUEST_${accNo}_$version"
    }
}
