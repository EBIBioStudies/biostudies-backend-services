package ebi.ac.uk.commons.http.slack

internal data class Notification(val text: String, val attachments: List<Attachment> = emptyList())

internal data class Attachment(
    val fallback: String,
    val color: String,
    val pretext: String,
    val text: String? = null,
    val fields: List<Field> = emptyList(),
)

internal data class Field(val title: String, val value: String)
