package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingConfig
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun FileProcessingConfig.fireCopy(fireFile: FireFile): ExtFile {
    logger.info { "copying fire file ${fireFile.fileName} with id ${fireFile.fireId}" }
    return fireFile
}

fun FileProcessingConfig.fireMove(fireFile: FireFile): ExtFile {
    logger.info { "moving fire file ${fireFile.fileName} with id ${fireFile.fireId}" }
    return fireFile
}
