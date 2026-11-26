package uk.ac.ebi.biostd.model

data class User(
    val id: Long,
    val email: String,
    val secretKey: String,
    val fullName: String,
    val notificationsEnabled: Boolean,
)
