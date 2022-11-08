package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.javaCmd
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmissionReleaserPropertiesTest {
    @BeforeEach
    fun beforeEach() {
        mockkStatic(::javaCmd)
        every { javaCmd(any()) } answers { listOf("java debug=${firstArg<Int?>()}") }
    }

    @Test
    fun `as java command`() {
        val properties = SubmissionReleaserProperties.create(
            mode = NOTIFY,
            databaseName = "dev",
            databaseUri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
            rabbitMqHost = "localhost",
            rabbitMqUser = "manager",
            rabbitMqPassword = "manager-local",
            rabbitMqPort = 5672,
            bioStudiesUrl = "http://localhost:8080",
            bioStudiesUser = "admin_user@ebi.ac.uk",
            bioStudiesPassword = "123456",
            firstWarningDays = 60,
            secondWarningDays = 30,
            thirdWarningDays = 7
        )

        assertThat(properties.asCmd("/apps-folder", 8569)).isEqualTo(
            """
            "java debug=8569 \
            -jar /apps-folder/submission-releaser-task-1.0.0.jar \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --spring.data.mongodb.database=dev \
            --spring.rabbitmq.host=localhost \
            --spring.rabbitmq.username=manager \
            --spring.rabbitmq.password=manager-local \
            --spring.rabbitmq.port=5672 \
            --app.mode=NOTIFY \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456 \
            --app.notification-times.first-warning-days=60 \
            --app.notification-times.second-warning-days=30 \
            --app.notification-times.third-warning-days=7"
            """.trimIndent()
        )
    }
}
