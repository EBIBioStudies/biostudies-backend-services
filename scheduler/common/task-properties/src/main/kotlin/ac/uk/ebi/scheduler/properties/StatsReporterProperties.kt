package ac.uk.ebi.scheduler.properties

import ac.uk.ebi.scheduler.common.JavaAppProperties
import ac.uk.ebi.scheduler.common.javaCmd

class StatsReporterProperties : JavaAppProperties {
    override fun asCmd(location: String, debugPort: Int?): String {
        return buildList {
            addAll(javaCmd(debugPort))
            add("-jar $location/$APP_NAME")
            add("--spring.data.mongodb.uri=$mongodbUri")
            add("--spring.data.mongodb.database=$mongodbDatabase")
            add("--app.outputPath=$outputPath")
            add("--app.publishPath=$publishPath")
            add("--app.ssh.user=$sshUser")
            add("--app.ssh.key=$sshKey")
            add("--app.ssh.server=$sshServer")
        }.joinToString(separator = " \\\n", prefix = "\"", postfix = "\"")
    }

    lateinit var mongodbUri: String
    lateinit var mongodbDatabase: String

    lateinit var outputPath: String
    lateinit var publishPath: String

    lateinit var sshUser: String
    lateinit var sshKey: String
    lateinit var sshServer: String

    companion object {
        private const val APP_NAME = "stats-reporter-task-1.0.0.jar"

        @Suppress("LongParameterList")
        fun create(
            databaseName: String,
            databaseUri: String,
            outputPath: String,
            publishPath: String,
            sshUser: String,
            sshKey: String,
            sshServer: String,
        ) = StatsReporterProperties().apply {
            this.mongodbDatabase = databaseName
            this.mongodbUri = databaseUri
            this.outputPath = outputPath
            this.publishPath = publishPath
            this.sshUser = sshUser
            this.sshKey = sshKey
            this.sshServer = sshServer
        }
    }
}
