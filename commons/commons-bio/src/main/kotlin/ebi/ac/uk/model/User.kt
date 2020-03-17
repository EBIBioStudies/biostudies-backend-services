package ebi.ac.uk.model

data class User(val id: Long, val email: String, val secretKey: String, val fullName: String? = null)
