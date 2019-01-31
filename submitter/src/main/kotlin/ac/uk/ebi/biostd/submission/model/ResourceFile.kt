package ac.uk.ebi.biostd.submission.model

import java.io.InputStream

data class ResourceFile(val name: String, val inputStream: InputStream, val size: Long)
