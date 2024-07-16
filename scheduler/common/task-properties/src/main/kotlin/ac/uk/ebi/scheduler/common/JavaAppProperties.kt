package ac.uk.ebi.scheduler.common

interface JavaAppProperties {
    fun asCmd(
        location: String,
        debugPort: Int?,
    ): String
}

fun javaCmd(port: Int?): List<String> =
    buildList {
        add("module load openjdk-17.0.5_8-gcc-11.2.0-gsv4jnu;")
        add("java")
        add("-Dsun.jnu.encoding=UTF-8 -Xmx6g")
        port?.let { add("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:$it,suspend=y") }
    }
