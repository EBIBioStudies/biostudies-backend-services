package ebi.ac.uk.model

// TODO fullname shoudln't be nullable
data class User(val id: Long, val email: String, val secretKey: String, val fullName: String? = null)
