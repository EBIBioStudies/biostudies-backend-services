package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty

private const val APP_NAME = "pmc-processor-task-1.0.0.jar"

class PmcImporterProperties : BaseAppProperty {

    override fun asJavaCommand(location: String) =
        StringBuilder().apply {
            append("java -jar $location/$APP_NAME \\\n")
            append("--app.data.mode=$mode \\\n")
            append("--app.data.temp=$temp \\\n")
            append("--app.data.mongodbUri=$mongodbUri \\\n")

            path?.let { append("--app.data.path=$it \\\n") }
            bioStudiesUrl?.let { append("--app.data.bioStudiesUrl=$it \\\n") }
            bioStudiesUser?.let { append("--app.data.bioStudiesUser=$it \\\n") }
            bioStudiesPassword?.let { append("--app.data.bioStudiesPassword=$it \\\n") }
        }.removeSuffix(" \\\n").toString()

    lateinit var mode: PmcMode
    lateinit var temp: String
    lateinit var mongodbUri: String
    var path: String? = null
    var bioStudiesUrl: String? = null
    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null

    companion object {

        // Todo: refactor to have biostudies parameters in a wrapper class
        @Suppress("LongParameterList")
        fun create(
            mode: PmcMode,
            path: String?,
            temp: String,
            mongodbUri: String,
            bioStudiesUrl: String? = null,
            bioStudiesUser: String? = null,
            bioStudiesPassword: String? = null
        ) = PmcImporterProperties().apply {
            this.mode = mode
            this.path = path
            this.temp = temp
            this.mongodbUri = mongodbUri
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
        }
    }
}

enum class PmcMode {
    LOAD, PROCESS, SUBMIT
}
