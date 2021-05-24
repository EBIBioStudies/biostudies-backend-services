package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty
import java.lang.StringBuilder

private const val APP_NAME = "exporter-task-1.0.0.jar"

class ExporterProperties : BaseAppProperty {
    override fun asJavaCommand(location: String): String =
        StringBuilder().apply {
            append("java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar $location/$APP_NAME \\\n")
            append("--app.fileName=$fileName \\\n")
            append("--app.outputPath=$outputPath \\\n")
            append("--app.bioStudies.url=$bioStudiesUrl \\\n")
            append("--app.bioStudies.user=$bioStudiesUser \\\n")
            append("--app.bioStudies.password=$bioStudiesPassword \\\n")
        }.removeSuffix(" \\\n").toString()

    lateinit var fileName: String
    lateinit var outputPath: String
    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String

    companion object {
        fun create(
            fileName: String,
            outputPath: String,
            bioStudiesUrl: String,
            bioStudiesUser: String,
            bioStudiesPassword: String
        ) = ExporterProperties().apply {
            this.fileName = fileName
            this.outputPath = outputPath
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
        }
    }
}
