package ac.uk.ebi.biostd.persistence.doc.db.lock

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Duration

internal class DistributedLockExecutor(
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    @Suppress("SwallowedException")
    suspend fun acquireLock(
        lockIdentifier: String,
        lockOwner: String,
        expiration: Duration = DEFAULT_EXPIRATION,
    ): Boolean {
        val query = Query.query(where(LOCK_ID).`is`(lockIdentifier).and(EXPIRES).lte(System.currentTimeMillis()))
        val update =
            Update()
                .set(EXPIRES, System.currentTimeMillis() + expiration.toMillis())
                .set(OWNER, lockOwner)
                .setOnInsert(LOCK_ID, lockIdentifier)
        val options = FindAndModifyOptions.options().returnNew(true).upsert(true)

        try {
            val acquiredLock =
                mongoTemplate
                    .findAndModify(query, update, options, Lock::class.java)
                    .awaitFirstOrNull()
            return if (acquiredLock != null) true else false
        } catch (e: DuplicateKeyException) {
            return false
        }
    }

    suspend fun releaseLock(
        lockIdentifier: String,
        lockOwner: String,
    ): Boolean {
        val query = Query.query(where(LOCK_ID).`is`(lockIdentifier).and(OWNER).`is`(lockOwner))
        val result = mongoTemplate.remove(query, Lock::class.java).awaitSingle()
        return result.deletedCount > 0
    }

    data class Lock(
        @Id
        val lockId: String,
        val expires: Long,
        val owner: String,
    )

    private companion object {
        val DEFAULT_EXPIRATION = Duration.ofMinutes(5)
        const val OWNER = "owner"
        const val EXPIRES = "expires"
        const val LOCK_ID = "_id"
    }
}
