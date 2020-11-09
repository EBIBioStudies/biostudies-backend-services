package ebi.ac.uk.notifications.model

class Email(
    val from: String,
    val to: String,
    val subject: String,
    val content: String,
    val html: Boolean = true
)
