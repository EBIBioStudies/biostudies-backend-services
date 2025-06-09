package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.data.LinkListDocLinkDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ebi.ac.uk.coroutines.every
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

private val logger = KotlinLogging.logger {}

class ToExtLinkListMapper(
    private val extFilesResolver: FilesResolver,
    private val serializationService: ExtSerializationService,
    private val linkListDocLinkDocDataRepository: LinkListDocLinkDocDataRepository,
) {
    @Suppress("LongParameterList")
    suspend fun toExtLinkList(
        linkList: DocLinkList,
        accNo: String,
        version: Int,
        released: Boolean,
        subRelPath: String,
        includeLinkListLinks: Boolean,
    ): ExtLinkList {
        fun linkListLinks(): Flow<ExtLink> =
            linkListDocLinkDocDataRepository
                .findByLinkList(accNo, version, linkList.fileName)
                .map { it.link.toExtLink() }
                .flowOn(Dispatchers.Default)

        val links = if (includeLinkListLinks) linkListLinks() else emptyFlow()

        return ExtLinkList(
            filePath = linkList.fileName,
            file = writeLinkList(accNo, version, linkList.fileName, links),
            pageTabFiles = linkList.pageTabFiles.map { it.toExtFile(released, subRelPath) },
        )
    }

    private suspend fun writeLinkList(
        accNo: String,
        version: Int,
        name: String,
        links: Flow<ExtLink>,
    ): File {
        fun asLoggableFlow(links: Flow<ExtLink>): Flow<ExtLink> =
            links.every(
                items = 500,
            ) { logger.info { "accNo:'$accNo' version: '$version', serialized link ${it.index}, link list '$name'" } }

        logger.info { "accNo:'$accNo' version: '$version', serializing link list '$name'" }
        val file = extFilesResolver.createRequestTempFile(accNo, version, name)
        file.outputStream().use { serializationService.serializeLinks(asLoggableFlow(links), it) }
        logger.info { "accNo:'$accNo' version: '$version', completed link list '$name' serialization" }
        return file
    }
}
