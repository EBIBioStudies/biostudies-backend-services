package ac.uk.ebi.scheduler.common

interface BaseAppProperty {

    fun asJavaCommand(location: String): String
}
