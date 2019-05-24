package ebi.ac.uk.notifications.integration.model

/**
 * NotificationType, represent a group of notification that share the same sender, subjectt and content template.
 */
class NotificationType<T : NotificationTemplateModel>(
    val from: String,
    val subject: String,
    val template: NotificationTemplate<T>
)

/**
 * Notification, represent a single notification to a single reciever.
 */
class Notification<T : NotificationTemplateModel>(val receiver: String, val templateModel: T)
