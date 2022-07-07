package ac.uk.ebi.scheduler.common

interface BaseAppProperty {
    fun asJavaCommand(location: String, javaHome: String): String
}
