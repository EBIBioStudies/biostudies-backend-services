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

    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null
    var submissionId: String? = null

    var loadFolder: String? = null
    var loadFile: String? = null
    var sourceFile: String? = null
    var limit: Int? = null
    var bioStudiesUrl: String? = null

    override fun asCmd(
        location: String,
        debugPort: Int?,
    ): String =
        buildList {
            addAll(javaCmd(debugPort))
            add("-jar $location/$APP_NAME")
            add("--app.data.mode=$mode")
            add("--app.data.temp=$temp")
            add("--app.data.mongodbUri=$mongodbUri")
            add("--app.data.mongodbDatabase=$mongodbDatabase")
            add("--app.data.notificationsUrl=$notificationsUrl")
            add("--app.data.pmcBaseUrl=$pmcBaseUrl")

            bioStudiesUrl?.let { add("--app.data.bioStudiesUrl=$it") }
            bioStudiesUser?.let { add("--app.data.bioStudiesUser=$it") }
            bioStudiesPassword?.let { add("--app.data.bioStudiesPassword=$it") }

            loadFolder?.let { add("--app.data.loadFolder=$it") }
            loadFile?.let { add("--app.data.loadFile=$it") }
            sourceFile?.let { add("--app.data.sourceFile=$it") }
            submissionId?.let { add("--app.data.submissionId=$it") }
            limit?.let { add("--app.data.limit=$it") }
        }.joinToString(separator = " \\\n")

    companion object {
        // Todo: refactor to have biostudies parameters in a wrapper class
        @Suppress("LongParameterList")
        fun create(
            mode: PmcMode,
            loadFolder: String?,
            loadFile: String? = null,
            sourceFile: String? = null,
            temp: String,
            mongodbUri: String,
            mongodbDatabase: String,
            notificationsUrl: String,
            pmcBaseUrl: String,
            bioStudiesUrl: String? = null,
            bioStudiesUser: String? = null,
            bioStudiesPassword: String? = null,
            submissionId: String? = null,
        ) = PmcImporterProperties().apply {
            this.mode = mode
            this.loadFolder = loadFolder
            this.loadFile = loadFile
            this.sourceFile = sourceFile
            this.temp = temp
            this.mongodbUri = mongodbUri
            this.mongodbDatabase = mongodbDatabase
            this.notificationsUrl = notificationsUrl
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
            this.submissionId = submissionId
            this.pmcBaseUrl = pmcBaseUrl
        }
    }
}

enum class PmcMode {
    LOAD,
    PROCESS,
    SUBMIT,
    SUBMIT_SINGLE,
    ;

    val description: String
        get() =
            when (this) {
                LOAD -> "PMC Submissions loading"
                PROCESS -> "PMC Submissions processing"
                SUBMIT -> "PMC Submissions submitting"
                SUBMIT_SINGLE -> "PMC Single submission submitting"
            }
}
