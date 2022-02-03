package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JAVA_HOME
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubmissionReleaserPropertiesTest {
    @Test
    fun `as java command`() {
        val properties = SubmissionReleaserProperties.create(
            mode = NOTIFY,
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

        assertThat(properties.asJavaCommand("/apps-folder")).isEqualTo(
            """
            $JAVA_HOME/bin/java -Dsun.jnu.encoding=UTF-8 -jar /apps-folder/submission-releaser-task-1.0.0.jar \
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
            --app.notification-times.third-warning-days=7
            """.trimIndent()
        )
    }
}
