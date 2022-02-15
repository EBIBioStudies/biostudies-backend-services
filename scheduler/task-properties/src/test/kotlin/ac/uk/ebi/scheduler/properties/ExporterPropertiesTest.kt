package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JAVA_HOME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExporterPropertiesTest {
    @Test
    fun `as java command`() {
        val properties = ExporterProperties.create(
            fileName = "publicOnlyStudies",
            outputPath = "/an/output/path",
            bioStudiesUrl = "http://localhost:8080",
            bioStudiesUser = "admin_user@ebi.ac.uk",
            bioStudiesPassword = "123456"
        )

        assertThat(properties.asJavaCommand("/apps-folder")).isEqualTo(
            """
            $JAVA_HOME/bin/java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar /apps-folder/exporter-task-1.0.0.jar \
            --app.fileName=publicOnlyStudies \
            --app.outputPath=/an/output/path \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456
            """.trimIndent()
        )
    }
}
