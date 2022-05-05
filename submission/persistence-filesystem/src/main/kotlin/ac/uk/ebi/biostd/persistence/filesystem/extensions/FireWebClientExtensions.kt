package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtFileType
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal fun FireWebClient.getOrPersist(
    accNo: String,
    file: File,
    type: ExtFileType,
    md5: String,
    path: String
): FireApiFile = findByPath(path) ?: persistFireFile(accNo, file, type, md5, path)

internal fun FireWebClient.persistFireFile(
    accNo: String,
    file: File,
    type: ExtFileType,
    md5: String,
    path: String
): FireApiFile {
    val fireFile = save(file, md5)
    setBioMetadata(fireFile.fireOid, accNo, type.value, published = false)
    setPath(fireFile.fireOid, path)

    return fireFile
}
