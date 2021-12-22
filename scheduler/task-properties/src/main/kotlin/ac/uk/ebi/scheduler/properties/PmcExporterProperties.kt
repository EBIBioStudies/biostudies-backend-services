package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty
import java.lang.StringBuilder

private const val APP_NAME = "pmc-exporter-task-1.0.0.jar"

@Suppress("LongParameterList")
class PmcExporterProperties : BaseAppProperty {
    override fun asJavaCommand(location: String): String =
        StringBuilder().apply {
            append("java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar $location/$APP_NAME \\\n")
            append("--app.fileName=$fileName \\\n")
            append("--app.outputPath=$outputPath \\\n")
            append("--app.ftp.host=$ftpHost \\\n")
            append("--app.ftp.user=$ftpUser \\\n")
            append("--app.ftp.password=$ftpPassword \\\n")
            append("--app.ftp.port=$ftpPort \\\n")
            append("--app.persistence.enableMongo=true \\\n")
            append("--spring.data.mongodb.database=$databaseName \\\n")
            append("--spring.data.mongodb.uri=$databaseUri \\\n")
        }.removeSuffix(" \\\n").toString()

    lateinit var fileName: String
    lateinit var outputPath: String
    lateinit var ftpHost: String
    lateinit var ftpUser: String
    lateinit var ftpPassword: String
    lateinit var ftpPort: Number
    lateinit var databaseName: String
    lateinit var databaseUri: String

    companion object {
        fun create(
            fileName: String,
            outputPath: String,
            ftpHost: String,
            ftpUser: String,
            ftpPassword: String,
            ftpPort: Number,
            databaseName: String,
            databaseUri: String
        ) = PmcExporterProperties().apply {
            this.fileName = fileName
            this.outputPath = outputPath
            this.ftpHost = ftpHost
            this.ftpUser = ftpUser
            this.ftpPassword = ftpPassword
            this.ftpPort = ftpPort
            this.databaseName = databaseName
            this.databaseUri = databaseUri
        }
    }
}
