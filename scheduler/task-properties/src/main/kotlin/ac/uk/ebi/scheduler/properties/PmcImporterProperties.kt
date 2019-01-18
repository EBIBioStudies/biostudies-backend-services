package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty

class PmcImporterProperties() : BaseAppProperty {

    override fun asJavaCommand(location: String): String {
        return """java -jar $location/pmc-importer-task.jar \
            --app.data.path=$path \
            --app.data.mode=$mode \
            --app.data.temp=$temp \
            --app.data.mongodbUri=$mongodbUri
             """.trimIndent()
    }

    constructor(mode: ImportMode, path: String, temp: String, mongodbUri: String) : this() {
        this.mode = mode
        this.path = path
        this.temp = temp
        this.mongodbUri = mongodbUri
    }

    lateinit var mode: ImportMode
    lateinit var path: String
    lateinit var temp: String
    lateinit var mongodbUri: String
}

enum class ImportMode {
    FILE, GZ_FILE
}
