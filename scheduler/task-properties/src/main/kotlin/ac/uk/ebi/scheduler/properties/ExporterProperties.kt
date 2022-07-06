package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty
import ac.uk.ebi.scheduler.common.JAVA_HOME
import java.lang.StringBuilder

private const val APP_NAME = "exporter-task-1.0.0.jar"

@Suppress("LongParameterList")
class ExporterProperties : BaseAppProperty {
    override fun asJavaCommand(location: String): String =
        StringBuilder().apply {
            append("$JAVA_HOME/bin/java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar $location/$APP_NAME \\\n")
            append("--app.mode=$mode \\\n")
            append("--app.fileName=$fileName \\\n")
            append("--app.outputPath=$outputPath \\\n")
            append("--app.tmpFilesPath=$tmpFilesPath \\\n")
            append("--app.ftp.host=$ftpHost \\\n")
            append("--app.ftp.user=$ftpUser \\\n")
            append("--app.ftp.password=$ftpPassword \\\n")
            append("--app.ftp.port=$ftpPort \\\n")
            append("--spring.data.mongodb.database=$databaseName \\\n")
            append("--spring.data.mongodb.uri=$databaseUri \\\n")
            append("--app.bioStudies.url=$bioStudiesUrl \\\n")
            append("--app.bioStudies.user=$bioStudiesUser \\\n")
            append("--app.bioStudies.password=$bioStudiesPassword \\\n")
        }.removeSuffix(" \\\n").toString()

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
            bioStudiesPassword: String
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
    PMC, PUBLIC_ONLY
}
