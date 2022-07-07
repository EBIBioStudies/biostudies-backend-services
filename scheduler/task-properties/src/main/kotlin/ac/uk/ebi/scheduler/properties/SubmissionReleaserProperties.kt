package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

private const val APP_NAME = "submission-releaser-task-1.0.0.jar"

class SubmissionReleaserProperties : JavaAppProperties {
    override fun asCmd(location: String, javaHome: String, debugPort: Int?): String =
        buildString {
            append(javaCmd(javaHome, debugPort))
            append("-jar $location/$APP_NAME \\\n")
            append("--spring.data.mongodb.uri=$mongodbUri \\\n")
            append("--spring.data.mongodb.database=$mongodbDatabase \\\n")
            append("--spring.rabbitmq.host=$rabbitMqHost \\\n")
            append("--spring.rabbitmq.username=$rabbitMqUser \\\n")
            append("--spring.rabbitmq.password=$rabbitMqPassword \\\n")
            append("--spring.rabbitmq.port=$rabbitMqPort \\\n")
            append("--app.mode=$mode \\\n")
            append("--app.bioStudies.url=$bioStudiesUrl \\\n")
            append("--app.bioStudies.user=$bioStudiesUser \\\n")
            append("--app.bioStudies.password=$bioStudiesPassword \\\n")
            append("--app.notification-times.first-warning-days=$firstWarningDays \\\n")
            append("--app.notification-times.second-warning-days=$secondWarningDays \\\n")
            append("--app.notification-times.third-warning-days=$thirdWarningDays \\\n")
        }.removeSuffix(" \\\n")

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
