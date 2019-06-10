package ebi.ac.uk.model

import java.nio.file.Path

data class User(val id: Long, val email: String, val secretKey: String, val magicFolder: Path)
