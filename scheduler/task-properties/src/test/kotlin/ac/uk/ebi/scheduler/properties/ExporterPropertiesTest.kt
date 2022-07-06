package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JAVA_HOME
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExporterPropertiesTest {
    @Test
    fun `public only mode as java command`() {
        val properties = ExporterProperties.create(
            mode = PUBLIC_ONLY,
            fileName = "publicOnlyStudies",
            outputPath = "/an/output/path",
            tmpFilesPath = "/a/tmp/path",
            ftpHost = "localhost",
            ftpUser = "admin",
            ftpPassword = "123456",
            ftpPort = 21,
            databaseName = "dev",
            databaseUri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
            bioStudiesUrl = "http://localhost:8080",
            bioStudiesUser = "admin_user@ebi.ac.uk",
            bioStudiesPassword = "123456"
        )

        assertThat(properties.asJavaCommand("/apps-folder")).isEqualTo(
            """
            $JAVA_HOME/bin/java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar /apps-folder/exporter-task-1.0.0.jar \
            --app.mode=PUBLIC_ONLY \
            --app.fileName=publicOnlyStudies \
            --app.outputPath=/an/output/path \
            --app.tmpFilesPath=/a/tmp/path \
            --app.ftp.host=localhost \
            --app.ftp.user=admin \
            --app.ftp.password=123456 \
            --app.ftp.port=21 \
            --spring.data.mongodb.database=dev \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456
            """.trimIndent()
        )
    }

    @Test
    fun `pmc mode as java command`() {
        val properties = ExporterProperties.create(
            mode = PMC,
            fileName = "publicOnlyStudies",
            outputPath = "/an/output/path",
            tmpFilesPath = "/a/tmp/path",
            ftpHost = "localhost",
            ftpUser = "admin",
            ftpPassword = "123456",
            ftpPort = 21,
            databaseName = "dev",
            databaseUri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
            bioStudiesUrl = "http://localhost:8080",
            bioStudiesUser = "admin_user@ebi.ac.uk",
            bioStudiesPassword = "123456"
        )

        assertThat(properties.asJavaCommand("/apps-folder")).isEqualTo(
            """
            $JAVA_HOME/bin/java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar /apps-folder/exporter-task-1.0.0.jar \
            --app.mode=PMC \
            --app.fileName=publicOnlyStudies \
            --app.outputPath=/an/output/path \
            --app.tmpFilesPath=/a/tmp/path \
            --app.ftp.host=localhost \
            --app.ftp.user=admin \
            --app.ftp.password=123456 \
            --app.ftp.port=21 \
            --spring.data.mongodb.database=dev \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456
            """.trimIndent()
        )
    }
}
