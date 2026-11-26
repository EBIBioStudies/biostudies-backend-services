package ac.uk.ebi.biostd.files.model

import uk.ac.ebi.biostd.api.UserFileType

data class FilesSpec(val files: List<UserFile>)

data class UserFile(val name: String, val path: String, val fileSize: Long, val type: UserFileType)
