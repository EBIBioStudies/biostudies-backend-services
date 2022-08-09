package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

private const val APP_NAME = "pmc-processor-task-1.0.0.jar"

class PmcImporterProperties : JavaAppProperties {

    lateinit var mode: PmcMode
    lateinit var temp: String
    lateinit var mongodbUri: String
    lateinit var mongodbDatabase: String
    lateinit var notificationsUrl: String
    lateinit var pmcBaseUrl: String

    var loadFolder: String? = null
    var bioStudiesUrl: String? = null
    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null
    var submissionId: String? = null

    override fun asCmd(location: String, javaHome: String, debugPort: Int?) =
        buildString {
            append(javaCmd(javaHome, debugPort))
            append("-jar $location/$APP_NAME \\\n")
            append("--app.data.mode=$mode \\\n")
            append("--app.data.temp=$temp \\\n")
            append("--app.data.mongodbUri=$mongodbUri \\\n")
            append("--app.data.mongodbDatabase=$mongodbDatabase \\\n")
            append("--app.data.notificationsUrl=$notificationsUrl \\\n")
            append("--app.data.pmcBaseUrl=$pmcBaseUrl \\\n")

            loadFolder?.let { append("--app.data.loadFolder=$it \\\n") }
            bioStudiesUrl?.let { append("--app.data.bioStudiesUrl=$it \\\n") }
            bioStudiesUser?.let { append("--app.data.bioStudiesUser=$it \\\n") }
            bioStudiesPassword?.let { append("--app.data.bioStudiesPassword=$it \\\n") }
        }.removeSuffix(" \\\n")

    companion object {

        // Todo: refactor to have biostudies parameters in a wrapper class
        @Suppress("LongParameterList")
        fun create(
            mode: PmcMode,
            loadFolder: String?,
            temp: String,
            mongodbUri: String,
            mongodbDatabase: String,
            notificationsUrl: String,
            pmcBaseUrl: String,
            bioStudiesUrl: String? = null,
            bioStudiesUser: String? = null,
            bioStudiesPassword: String? = null,
        ) = PmcImporterProperties().apply {
            this.mode = mode
            this.loadFolder = loadFolder
            this.temp = temp
            this.mongodbUri = mongodbUri
            this.mongodbDatabase = mongodbDatabase
            this.notificationsUrl = notificationsUrl
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
            this.pmcBaseUrl = pmcBaseUrl
        }
    }
}

enum class PmcMode {
    LOAD, PROCESS, SUBMIT, SUBMIT_SINGLE;

    val description: String
        get() = when (this) {
            LOAD -> "PMC Submissions loading"
            PROCESS -> "PMC Submissions processing"
            SUBMIT -> "PMC Submissions submitting"
            SUBMIT_SINGLE -> "PMC Submission submitting"
        }
}
