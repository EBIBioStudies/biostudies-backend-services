package ac.uk.ebi.scheduler.properties

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

const val path = "/loadPath"
const val tempDir = "/tempDir"
const val mongodbUri = "mongodbUri"
const val mongodbDatabase = "a-database"
const val bioStudiesUrl = "http://an_url.com"
const val bioStudiesUser = "user"
const val bioStudiesPassword = "password"
const val notificationUrl = "http://slack-here"
const val baseUrl = "http://pmc"

class PmcImporterPropertiesTest {
    @Test
    fun asJavaCommand() {
        val properties = PmcImporterProperties.create(
            mode = PmcMode.LOAD,
            loadFolder = path,
            temp = tempDir,
            mongodbUri = mongodbUri,
            mongodbDatabase = mongodbDatabase,
            bioStudiesUrl = bioStudiesUrl,
            bioStudiesUser = bioStudiesUser,
            bioStudiesPassword = bioStudiesPassword,
            pmcBaseUrl = baseUrl,
            notificationsUrl = notificationUrl)

        assertThat(properties.asJavaCommand("/apps-folder"))
            .isEqualTo("""
                java -jar /apps-folder/pmc-processor-task-1.0.0.jar \
                --app.data.mode=LOAD \
                --app.data.temp=/tempDir \
                --app.data.mongodbUri=mongodbUri \
                --app.data.mongodbDatabase=a-database \
                --app.data.notificationsUrl=http://slack-here \
                --app.data.pmcBaseUrl=http://pmc \
                --app.data.path=/loadPath \
                --app.data.bioStudiesUrl=http://an_url.com \
                --app.data.bioStudiesUser=user \
                --app.data.bioStudiesPassword=password
            """.trimIndent())
    }

    @Test
    fun `asJavaCommand when not optional parameter`() {
        val properties = PmcImporterProperties.create(
            mode = PmcMode.LOAD,
            loadFolder = null,
            temp = tempDir,
            mongodbUri = mongodbUri,
            mongodbDatabase = mongodbDatabase,
            bioStudiesUrl = null,
            bioStudiesUser = null,
            bioStudiesPassword = null,
            pmcBaseUrl = baseUrl,
            notificationsUrl = notificationUrl)
        assertThat(properties.asJavaCommand("/apps-folder"))
            .isEqualTo("""
            java -jar /apps-folder/pmc-processor-task-1.0.0.jar \
            --app.data.mode=LOAD \
            --app.data.temp=/tempDir \
            --app.data.mongodbUri=mongodbUri \
            --app.data.mongodbDatabase=a-database \
            --app.data.notificationsUrl=http://slack-here \
            --app.data.pmcBaseUrl=http://pmc
            """.trimIndent())
    }
}
