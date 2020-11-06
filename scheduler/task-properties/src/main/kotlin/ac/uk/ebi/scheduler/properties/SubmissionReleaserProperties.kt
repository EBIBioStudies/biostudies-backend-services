package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.BaseAppProperty

private const val APP_NAME = "submission-releaser-task-1.0.0.jar"

class SubmissionReleaserProperties : BaseAppProperty {
    override fun asJavaCommand(location: String): String =
        StringBuilder().apply {
            append("java -jar $location/$APP_NAME \\\n")
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
        }.removeSuffix(" \\\n").toString()

    lateinit var mode: ReleaserMode

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
            rabbitMqHost: String,
            rabbitMqUser: String,
            rabbitMqPassword: String,
            rabbitMqPort: Long,
            bioStudiesUrl: String,
            bioStudiesUser: String,
            bioStudiesPassword: String,
            firstWarningDays: Long,
            secondWarningDays: Long,
            thirdWarningDays: Long
        ) = SubmissionReleaserProperties().apply {
            this.mode = mode
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
    NOTIFY, RELEASE
}
