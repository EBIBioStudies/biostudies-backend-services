package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.javaCmd
import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

const val LOAD_FOLDER = "/loadPath"
const val LOAD_FILE = "import.gz"
const val TEMP_DIR = "/tempDir"
const val MONGODB_URI = "mongodbUri"
const val MONGODB_DATABASE = "a-database"
const val BIO_STUDIES_URL = "http://an_url.com"
const val BIO_STUDIES_USER = "user"
const val BIO_STUDIES_PASSWORD = "password"
const val NOTIFICATION_URL = "http://slack-here"
const val BASE_URL = "http://pmc"

class PmcImporterPropertiesTest {
    @BeforeEach
    fun beforeEach() {
        mockkStatic(::javaCmd)
        every { javaCmd(any()) } answers { listOf("java debug=${firstArg<Int?>()}") }
    }

    @Test
    fun asJavaCommand() {
        val properties =
            PmcImporterProperties.create(
                mode = PmcMode.LOAD,
                loadFolder = LOAD_FOLDER,
                loadFile = LOAD_FILE,
                temp = TEMP_DIR,
                mongodbUri = MONGODB_URI,
                mongodbDatabase = MONGODB_DATABASE,
                bioStudiesUrl = BIO_STUDIES_URL,
                bioStudiesUser = BIO_STUDIES_USER,
                bioStudiesPassword = BIO_STUDIES_PASSWORD,
                pmcBaseUrl = BASE_URL,
                notificationsUrl = NOTIFICATION_URL,
            )

        assertThat(properties.asCmd("/apps-folder", 8569))
            .isEqualTo(
                """
                java debug=8569 \
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
                --app.data.loadFile=import.gz
                """.trimIndent(),
            )
    }

    @Test
    fun `asJavaCommand when not optional parameter`() {
        val properties =
            PmcImporterProperties.create(
                mode = PmcMode.LOAD,
                loadFolder = null,
                temp = TEMP_DIR,
                mongodbUri = MONGODB_URI,
                mongodbDatabase = MONGODB_DATABASE,
                bioStudiesUrl = null,
                bioStudiesUser = null,
                bioStudiesPassword = null,
                pmcBaseUrl = BASE_URL,
                notificationsUrl = NOTIFICATION_URL,
            )
        assertThat(properties.asCmd("/apps-folder", 8569))
            .isEqualTo(
                """
                java debug=8569 \
                -jar /apps-folder/pmc-processor-task-1.0.0.jar \
                --app.data.mode=LOAD \
                --app.data.temp=/tempDir \
                --app.data.mongodbUri=mongodbUri \
                --app.data.mongodbDatabase=a-database \
                --app.data.notificationsUrl=http://slack-here \
                --app.data.pmcBaseUrl=http://pmc
                """.trimIndent(),
            )
    }
}
