package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty

class PmcImporterProperties() : BaseAppProperty {

    private val APP_NAME = "pmc-processor-task.jar"

    override fun asJavaCommand(location: String) =
        StringBuilder().apply {
            append("java -jar $location/$APP_NAME \\\n")
            append("--app.data.path=$path \\\n")
            append("--app.data.mode=$mode \\\n")
            append("--app.data.temp=$temp \\\n")
            append("--app.data.mongodbUri=$mongodbUri \\\n")

            bioStudiesUrl?.let { append("--app.data.bioStudiesUrl=$it \\\n") }
            bioStudiesUser?.let { append("--app.data.bioStudiesUser=$it \\\n") }
            bioStudiesPassword?.let { append("--app.data.bioStudiesPassword=$it \\\n") }
        }.removeSuffix(" \\\n").toString()

    constructor(
        mode: PmcMode,
        path: String?,
        temp: String,
        mongodbUri: String,
        bioStudiesUrl: String? = null,
        bioStudiesUser: String? = null,
        bioStudiesPassword: String? = null
    ) : this() {
        this.mode = mode
        this.path = path
        this.temp = temp
        this.mongodbUri = mongodbUri
        this.bioStudiesUrl = bioStudiesUrl
        this.bioStudiesUser = bioStudiesUser
        this.bioStudiesPassword = bioStudiesPassword
    }

    lateinit var mode: PmcMode
    lateinit var temp: String
    lateinit var mongodbUri: String
    var path: String? = null
    var bioStudiesUrl: String? = null
    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null
}

enum class PmcMode {
    LOAD, PROCESS, SUBMIT
}
