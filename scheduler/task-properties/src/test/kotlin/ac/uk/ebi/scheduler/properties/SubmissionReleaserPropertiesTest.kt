package ac.uk.ebi.scheduler.properties

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubmissionReleaserPropertiesTest {
    @Test
    fun `as java command`() {
        val properties = SubmissionReleaserProperties.create(
            rabbitMqHost = "localhost",
            rabbitMqUser = "manager",
            rabbitMqPassword = "manager-local",
            rabbitMqPort = 5672,
            bioStudiesUrl = "http://localhost:8080",
            bioStudiesUser = "admin_user@ebi.ac.uk",
            bioStudiesPassword = "123456",
            firstWarning = 60,
            secondWarning = 30,
            thirdWarning = 7)

        assertThat(properties.asJavaCommand("/apps-folder")).isEqualTo("""
            java -jar /apps-folder/submission-releaser-task-1.0.0.jar \
            --spring.rabbitmq.host=localhost \
            --spring.rabbitmq.username=manager \
            --spring.rabbitmq.password=manager-local \
            --spring.rabbitmq.port=5672 \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456 \
            --app.notification-times.first-warning=60 \
            --app.notification-times.second-warning=30 \
            --app.notification-times.third-warning=7
        """.trimIndent())
    }
}
