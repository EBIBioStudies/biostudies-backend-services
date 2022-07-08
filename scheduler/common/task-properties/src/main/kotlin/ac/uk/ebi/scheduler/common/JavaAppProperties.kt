package ac.uk.ebi.scheduler.common

interface JavaAppProperties {
    fun asCmd(location: String, javaHome: String, debugPort: Int?): String
}

fun javaCmd(javaHome: String, port: Int?): String = buildString {
    append("$javaHome/bin/java \\\n")
    append("-Dsun.jnu.encoding=UTF-8 -Xmx6g \\\n")
    port?.let { append("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:$it,suspend=y \\\n") }
}
