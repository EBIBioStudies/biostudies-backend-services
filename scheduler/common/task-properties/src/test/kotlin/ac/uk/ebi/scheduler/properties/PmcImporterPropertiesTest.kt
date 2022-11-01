package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.javaCmd
import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

const val loadFolder = "/loadPath"
const val loadFile = "import.gz"
const val tempDir = "/tempDir"
const val mongodbUri = "mongodbUri"
const val mongodbDatabase = "a-database"
const val bioStudiesUrl = "http://an_url.com"
const val bioStudiesUser = "user"
const val bioStudiesPassword = "password"
const val notificationUrl = "http://slack-here"
const val baseUrl = "http://pmc"

class PmcImporterPropertiesTest {
    @BeforeEach
    fun beforeEach() {
        mockkStatic(::javaCmd)
        every { javaCmd(any()) } answers { listOf("java debug=${firstArg<Int?>()}") }
    }

    @Test
    fun asJavaCommand() {
        val properties = PmcImporterProperties.create(
            mode = PmcMode.LOAD,
            loadFolder = loadFolder,
            loadFile = loadFile,
            temp = tempDir,
            mongodbUri = mongodbUri,
            mongodbDatabase = mongodbDatabase,
            bioStudiesUrl = bioStudiesUrl,
            bioStudiesUser = bioStudiesUser,
            bioStudiesPassword = bioStudiesPassword,
            pmcBaseUrl = baseUrl,
            notificationsUrl = notificationUrl
        )

        assertThat(properties.asCmd("/apps-folder", 8569))
            .isEqualTo(
                """
                "java debug=8569 \
                -jar /apps-folder/pmc-processor-task-1.0.0.jar \
                --app.data.mode=LOAD \
                --app.data.temp=/tempDir \
                --app.data.mongodbUri=mongodbUri \
                --app.data.mongodbDatabase=a-database \
                --app.data.notificationsUrl=http://slack-here \
                --app.data.pmcBaseUrl=http://pmc \
                --app.data.bioStudiesUrl=http://an_url.com \
                --app.data.bioStudiesUser=user \
                --app.data.bioStudiesPassword=password \
                --app.data.loadFolder=/loadPath \
                --app.data.loadFile=import.gz"
                """.trimIndent()
            )
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
            notificationsUrl = notificationUrl
        )
        assertThat(properties.asCmd("/apps-folder", 8569))
            .isEqualTo(
                """
            "java debug=8569 \
            -jar /apps-folder/pmc-processor-task-1.0.0.jar \
            --app.data.mode=LOAD \
            --app.data.temp=/tempDir \
            --app.data.mongodbUri=mongodbUri \
            --app.data.mongodbDatabase=a-database \
            --app.data.notificationsUrl=http://slack-here \
            --app.data.pmcBaseUrl=http://pmc"
                """.trimIndent()
            )
    }
}
