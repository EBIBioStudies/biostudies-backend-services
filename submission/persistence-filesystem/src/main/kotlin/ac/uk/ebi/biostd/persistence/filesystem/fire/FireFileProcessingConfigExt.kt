package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingConfig
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun FileProcessingConfig.fireCopy(fireFile: FireFile): ExtFile {
    logger.info { "$accNo $owner Copying fire file ${fireFile.fileName} with id ${fireFile.fireId}" }
    return fireFile
}

fun FileProcessingConfig.fireMove(fireFile: FireFile): ExtFile {
    logger.info { "$accNo $owner Moving fire file ${fireFile.fileName} with id ${fireFile.fireId}" }
    return fireFile
}
