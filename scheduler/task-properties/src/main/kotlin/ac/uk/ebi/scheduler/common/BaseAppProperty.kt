package ac.uk.ebi.scheduler.common

const val JAVA_HOME = "/nfs/biostudies/.adm/java/zulu11.45.27-ca-jdk11.0.10-linux_x64"

interface BaseAppProperty {
    fun asJavaCommand(location: String): String
}
