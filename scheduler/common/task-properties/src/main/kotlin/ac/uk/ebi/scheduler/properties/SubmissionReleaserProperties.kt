package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

private const val APP_NAME = "submission-releaser-task-1.0.0.jar"

class SubmissionReleaserProperties : JavaAppProperties {
    override fun asCmd(
        location: String,
        debugPort: Int?,
    ): String =
        buildList {
            addAll(javaCmd(debugPort))
            add("-jar $location/$APP_NAME")
            add("--spring.data.mongodb.uri=$mongodbUri")
            add("--spring.data.mongodb.database=$mongodbDatabase")
            add("--spring.rabbitmq.host=$rabbitMqHost")
            add("--spring.rabbitmq.username=$rabbitMqUser")
            add("--spring.rabbitmq.password=$rabbitMqPassword")
            add("--spring.rabbitmq.port=$rabbitMqPort")
            add("--app.mode=$mode")
            add("--app.bioStudies.url=$bioStudiesUrl")
            add("--app.bioStudies.user=$bioStudiesUser")
            add("--app.bioStudies.password=$bioStudiesPassword")
            add("--app.notification-times.first-warning-days=$firstWarningDays")
            add("--app.notification-times.second-warning-days=$secondWarningDays")
            add("--app.notification-times.third-warning-days=$thirdWarningDays")
        }.joinToString(separator = " \\\n")

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
    NOTIFY,
    RELEASE,
    GENERATE_FTP_LINKS,
}
