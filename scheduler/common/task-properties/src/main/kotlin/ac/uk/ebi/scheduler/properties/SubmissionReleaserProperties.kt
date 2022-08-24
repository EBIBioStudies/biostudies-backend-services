package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

private const val APP_NAME = "submission-releaser-task-1.0.0.jar"

class SubmissionReleaserProperties : JavaAppProperties {
    override fun asCmd(location: String, javaHome: String, debugPort: Int?): String =
        buildString {
            append(javaCmd(javaHome, debugPort))
            appendLine("-jar $location/$APP_NAME \\")
            appendLine("--spring.data.mongodb.uri=$mongodbUri \\")
            appendLine("--spring.data.mongodb.database=$mongodbDatabase \\")
            appendLine("--spring.rabbitmq.host=$rabbitMqHost \\")
            appendLine("--spring.rabbitmq.username=$rabbitMqUser \\")
            appendLine("--spring.rabbitmq.password=$rabbitMqPassword \\")
            appendLine("--spring.rabbitmq.port=$rabbitMqPort \\")
            appendLine("--app.mode=$mode \\")
            appendLine("--app.bioStudies.url=$bioStudiesUrl \\")
            appendLine("--app.bioStudies.user=$bioStudiesUser \\")
            appendLine("--app.bioStudies.password=$bioStudiesPassword \\")
            appendLine("--app.notification-times.first-warning-days=$firstWarningDays \\")
            appendLine("--app.notification-times.second-warning-days=$secondWarningDays \\")
            append("--app.notification-times.third-warning-days=$thirdWarningDays")
        }

    lateinit var mode: ReleaserMode

    lateinit var mongodbUri: String
    lateinit var mongodbDatabase: String

    lateinit var rabbitMqHost: String
    lateinit var rabbitMqUser: String
    lateinit var rabbitMqPassword: String
    var rabbitMqPort: Long = -1

    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String

    var firstWarningDays: Long = -1
    var secondWarningDays: Long = -1
    var thirdWarningDays: Long = -1

    companion object {
        @Suppress("LongParameterList")
        fun create(
            mode: ReleaserMode,
            databaseName: String,
            databaseUri: String,
            rabbitMqHost: String,
            rabbitMqUser: String,
            rabbitMqPassword: String,
            rabbitMqPort: Long,
            bioStudiesUrl: String,
            bioStudiesUser: String,
            bioStudiesPassword: String,
            firstWarningDays: Long,
            secondWarningDays: Long,
            thirdWarningDays: Long,
        ) = SubmissionReleaserProperties().apply {
            this.mode = mode
            this.mongodbUri = databaseUri
            this.mongodbDatabase = databaseName
            this.rabbitMqHost = rabbitMqHost
            this.rabbitMqUser = rabbitMqUser
            this.rabbitMqPassword = rabbitMqPassword
            this.rabbitMqPort = rabbitMqPort
            this.bioStudiesUrl = bioStudiesUrl
            this.bioStudiesUser = bioStudiesUser
            this.bioStudiesPassword = bioStudiesPassword
            this.firstWarningDays = firstWarningDays
            this.secondWarningDays = secondWarningDays
            this.thirdWarningDays = thirdWarningDays
        }
    }
}

enum class ReleaserMode {
    NOTIFY, RELEASE, GENERATE_FTP_LINKS
}
