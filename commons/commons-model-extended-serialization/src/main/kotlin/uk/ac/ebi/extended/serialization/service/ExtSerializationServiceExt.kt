package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles
import java.util.concurrent.atomic.AtomicInteger

fun ExtSerializationService.forEachFile(
    submission: ExtSubmission,
    function: (ExtFile, Int) -> Unit,
) {
    val index = AtomicInteger()
    for (fileList in submission.allFileList) {
        fileList.file.inputStream().use { deserializeList(it).forEach { function(it, index.incrementAndGet()) } }
        fileList.pageTabFiles.forEach { function(it, index.incrementAndGet()) }
    }
    submission.allSectionsFiles.forEach { function(it, index.incrementAndGet()) }
    submission.pageTabFiles.forEach { function(it, index.incrementAndGet()) }
}
