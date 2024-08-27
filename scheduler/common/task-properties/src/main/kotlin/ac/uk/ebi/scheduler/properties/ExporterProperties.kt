package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

private const val APP_NAME = "exporter-task-1.0.0.jar"

@Suppress("LongParameterList")
class ExporterProperties : JavaAppProperties {
    override fun asCmd(
        location: String,
        debugPort: Int?,
    ): String =
        buildList {
            addAll(javaCmd(debugPort))
            add("-jar $location/$APP_NAME")
            add("--app.mode=$mode")
            add("--app.fileName=$fileName")
            add("--app.outputPath=$outputPath")
            add("--app.tmpFilesPath=$tmpFilesPath")
            add("--app.ftp.host=$ftpHost")
            add("--app.ftp.user=$ftpUser")
            add("--app.ftp.password=$ftpPassword")
            add("--app.ftp.port=$ftpPort")
            add("--spring.data.mongodb.database=$databaseName")
            add("--spring.data.mongodb.uri=$databaseUri")
            add("--app.bioStudies.url=$bioStudiesUrl")
            add("--app.bioStudies.user=$bioStudiesUser")
            add("--app.bioStudies.password=$bioStudiesPassword")
        }.joinToString(separator = " \\\n")

    lateinit var mode: ExporterMode

    lateinit var fileName: String
    lateinit var outputPath: String
    lateinit var tmpFilesPath: String

    lateinit var ftpHost: String
    lateinit var ftpUser: String
    lateinit var ftpPassword: String
    lateinit var ftpPort: Number

    lateinit var databaseName: String
    lateinit var databaseUri: String

    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String

    companion object {
        fun create(
            fileName: String,
            outputPath: String,
            tmpFilesPath: String,
            mode: ExporterMode,
            ftpHost: String,
            ftpUser: String,
            ftpPassword: String,
            ftpPort: Number,
            databaseName: String,
            databaseUri: String,
            bioStudiesUrl: String,
            bioStudiesUser: String,
            bioStudiesPassword: String,
        ) = ExporterProperties().apply {
            this.mode = mode
            this.fileName = fileName
            this.outputPath = outputPath
            this.tmpFilesPath = tmpFilesPath
            this.ftpHost = ftpHost
            this.ftpUser = ftpUser
            this.ftpPassword = ftpPassword
            this.ftpPort = ftpPort
            this.databaseName = databaseName
            this.databaseUri = databaseUri
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
        }
    }
}

enum class ExporterMode {
    PMC,
    PUBLIC_ONLY,
}
