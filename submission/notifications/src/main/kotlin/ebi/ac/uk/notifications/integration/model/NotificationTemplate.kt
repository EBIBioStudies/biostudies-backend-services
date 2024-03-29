package ebi.ac.uk.notifications.integration.model

/**
 * Represent a template in which a notification is based on.
 */
open class NotificationTemplate<T : NotificationTemplateModel>(private var template: String) {
    /**
     * Obtaining the string representation by replacing model properties into template string.
     */
    fun render(model: T): String = render(model.getParams())

    private fun render(params: List<Pair<String, String>>): String =
        params.fold(template) { content, (key, value) -> content.replace("\${$key}", value) }
}

/**
 * Contains the keys used to replace into the the template.
 */
interface NotificationTemplateModel {
    fun getParams(): List<Pair<String, String>>
}
