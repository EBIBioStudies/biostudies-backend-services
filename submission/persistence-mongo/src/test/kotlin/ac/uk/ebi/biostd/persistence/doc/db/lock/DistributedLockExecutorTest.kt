package ac.uk.ebi.biostd.persistence.doc.db.lock

import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.lang.Thread.sleep
import java.time.Duration

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class DistributedLockExecutorTest(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    private val testInstance = DistributedLockExecutor(mongoTemplate)

    @Test
    fun adquireLockWhenAvailable() =
        runTest {
            val lock = testInstance.acquireLock("lockId_1", "owner")

            assertThat(lock).isTrue()
        }

    @Test
    fun adquireLockWhenLock() =
        runTest {
            val lock = testInstance.acquireLock("lockId_2", "owner")
            val lock2 = testInstance.acquireLock("lockId_2", "another_owner")

            assertThat(lock).isTrue()
            assertThat(lock2).isFalse()
        }

    @Test
    fun adquireLockWhenExpired() =
        runTest {
            val lock = testInstance.acquireLock("lockId_3", "owner", Duration.ofMillis(500))
            assertThat(lock).isTrue()

            val lockTry = testInstance.acquireLock("lockId_3", "owner2")
            assertThat(lockTry).isFalse()

            sleep(1000)
            val lockTr2 = testInstance.acquireLock("lockId_3", "owner2")
            assertThat(lockTr2).isTrue()
        }

    @Test
    fun adquireLockAfterRelease() =
        runTest {
            val lock = testInstance.acquireLock("lockId_4", "owner")
            val lockTry = testInstance.acquireLock("lockId_4", "another_owner")

            val released = testInstance.releaseLock("lockId_4", "owner")
            val lockTry2 = testInstance.acquireLock("lockId_4", "another_owner")

            assertThat(lock).isTrue()
            assertThat(lockTry).isFalse()
            assertThat(released).isTrue()
            assertThat(lockTry2).isTrue()
        }

    @Test
    fun anotherOwnerReleaseDoesNotRelease() =
        runTest {
            val lock = testInstance.acquireLock("lockId_5", "owner")
            val released = testInstance.releaseLock("lockId_5", "another_owner")

            assertThat(lock).isTrue()
            assertThat(released).isFalse()
        }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
