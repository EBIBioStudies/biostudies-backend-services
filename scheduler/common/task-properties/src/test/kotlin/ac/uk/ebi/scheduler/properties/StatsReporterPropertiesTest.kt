package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.javaCmd
import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StatsReporterPropertiesTest {
    @BeforeEach
    fun beforeEach() {
        mockkStatic(::javaCmd)
        every { javaCmd(any()) } answers { listOf("java debug=${firstArg<Int?>()}") }
    }

    @Test
    fun `stats reporter properties test`() {
        val properties = StatsReporterProperties.create(
            databaseName = "dev",
            databaseUri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
            outputPath = "/stats/output",
            publishPath = "/stats/publish",
            sshUser = "test-user",
            sshKey = "test-ssh-key",
            sshServer = "test-server",
        )

        assertThat(properties.asCmd("/apps-folder", 8569)).isEqualTo(
            """
            "java debug=8569 \
            -jar /apps-folder/stats-reporter-task-1.0.0.jar \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --spring.data.mongodb.database=dev \
            --app.outputPath=/stats/output \
            --app.publishPath=/stats/publish \
            --app.ssh.user=test-user \
            --app.ssh.key=test-ssh-key \
            --app.ssh.server=test-server"
            """.trimIndent()
        )
    }
}
