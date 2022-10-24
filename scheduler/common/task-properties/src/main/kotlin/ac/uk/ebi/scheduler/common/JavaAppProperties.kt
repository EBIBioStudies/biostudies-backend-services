package ac.uk.ebi.scheduler.common

interface JavaAppProperties {
    fun asCmd(location: String, javaHome: String, debugPort: Int?): String
}

fun javaCmd(javaHome: String, port: Int?): String = buildString {
    appendLine("$javaHome/bin/java \\")
    appendLine("-Dsun.jnu.encoding=UTF-8 -Xmx6g \\")
    port?.let { appendLine("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:$it,suspend=y \\") }
}
