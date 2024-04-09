import java.util.Locale.ROOT

tasks.register("setUpRabbitMQ") {
    project.ext["args"] = ""
    project.ext["linuxScript"] = "./SetUpRabbitMQ.sh"
    project.ext["windowsScript"] = "SetUpRabbitMQ.cmd"
    project.ext["workingDir"] = "./src/main/resources/setup/rabbitmq"

    finalizedBy("executeScript")
}

tasks.register("setUpFireMock") {
    project.ext["args"] = ""
    project.ext["linuxScript"] = "./SetUpFireMock.sh"
    project.ext["windowsScript"] = "SetUpFireMock.cmd"
    project.ext["workingDir"] = "./src/main/resources/setup/firemock"

    finalizedBy("executeScript")
}

tasks.register("setUpCleanDatabase") {
    project.ext["args"] = "clean"

    finalizedBy("setUpDatabase")
}

tasks.register("setUpTestDatabase") {
    project.ext["args"] = "test"

    finalizedBy("setUpDatabase")
}

tasks.register("setUpDatabase") {
    project.ext["linuxScript"] = "./SetUpDatabase.sh"
    project.ext["windowsScript"] = "SetUpDatabase.cmd"
    project.ext["workingDir"] = "./src/main/resources/setup/mysql"

    finalizedBy("executeScript")
}

tasks.register("setUpMongoDatabase") {
    project.ext["args"] = ""
    project.ext["linuxScript"] = "./SetUpMongoDatabase.sh"
    project.ext["windowsScript"] = ""
    project.ext["workingDir"] = "./src/main/resources/setup/mongo"

    finalizedBy("executeScript")
}

tasks.register<Exec>("executeScript") {
    workingDir = File(project.ext["workingDir"].toString())

    val args = project.ext["args"].toString()
    val linuxScript = project.ext["linuxScript"].toString()
    val windowsScript = project.ext["windowsScript"].toString()
    val os = System.getProperty("os.name").toLowerCase(ROOT)

    commandLine =
        when {
            os.contains("windows") -> mutableListOf("cmd", "/c", windowsScript, args)
            else -> mutableListOf(linuxScript, args)
        }
}
