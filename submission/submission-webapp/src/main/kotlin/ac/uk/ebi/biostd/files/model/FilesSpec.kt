package ac.uk.ebi.biostd.files.model

import ebi.ac.uk.api.UserFileType

data class FilesSpec(val files: List<UserFile>)

data class UserFile(val name: String, val path: String, val fileSize: Long, val type: UserFileType)
