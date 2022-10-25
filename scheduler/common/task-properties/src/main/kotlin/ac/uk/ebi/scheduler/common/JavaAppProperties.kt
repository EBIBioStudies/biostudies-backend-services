package ac.uk.ebi.scheduler.common

interface JavaAppProperties {
    fun asCmd(location: String, debugPort: Int?): String
}

fun javaCmd(port: Int?): List<String> = buildList {
    add("module load openjdk-11.0.1-gcc-9.3.0-unymjzh;")
    add("java")
    add("-Dsun.jnu.encoding=UTF-8 -Xmx6g")
    port?.let { add("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:$it,suspend=y") }
}
