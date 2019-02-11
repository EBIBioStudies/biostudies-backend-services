package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty

class PmcImporterProperties() : BaseAppProperty {

    override fun asJavaCommand(location: String): String {
        return """java -jar $location/pmc-processor-task.jar \
            --app.data.path=$path \
            --app.data.mode=$mode \
            --app.data.temp=$temp \
            --app.data.mongodbUri=$mongodbUri \
            --app.data.bioStudiesUrl=$bioStudiesUrl \
            --app.data.bioStudiesUser=$bioStudiesUser \
            --app.data.bioStudiesPassword=$bioStudiesPassword
             """.trimIndent()
    }

    constructor(
        mode: PmcMode,
        path: String,
        temp: String,
        mongodbUri: String,
        bioStudiesUrl: String,
        bioStudiesUser: String,
        bioStudiesPassword: String
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
    lateinit var path: String
    lateinit var temp: String
    lateinit var mongodbUri: String
    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String
}

enum class PmcMode {
    LOAD, PROCESS, SUBMIT
}
