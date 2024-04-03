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

class DistributedLockExecutor(
    private val mongoTemplate: ReactiveMongoTemplate,
) {

    suspend fun acquireLock(
        lockIdentifier: String,
        lockOwner: String,
        expiration: Duration = DEFAULT_EXPIRATION,
    ): Boolean {
        val query = Query.query(where(lockId).`is`(lockIdentifier).and(expires).lte(System.currentTimeMillis()))
        val update = Update()
            .set(expires, System.currentTimeMillis() + expiration.toMillis())
            .set(owner, lockOwner)
            .setOnInsert(lockId, lockIdentifier)
        val options = FindAndModifyOptions.options().returnNew(true).upsert(true)

        try {
            val acquiredLock = mongoTemplate
                .findAndModify(query, update, options, Lock::class.java)
                .awaitFirstOrNull()
            return if (acquiredLock != null) true else false
        } catch (e: DuplicateKeyException) {
            return false
        }
    }

    suspend fun releaseLock(lockId: String, owner: String): Boolean {
        val query = Query.query(where(lockId).`is`(lockId).and(owner).`is`(owner))
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
        val owner = "owner"
        val expires = "expires"
        val lockId = "_id"
    }
}
