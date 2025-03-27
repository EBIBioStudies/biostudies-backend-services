package ebi.ac.uk.extended.mapping.from

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.io.use
import ebi.ac.uk.model.LinkList
import ebi.ac.uk.model.canonicalName
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class ToExtLinkListMapper(
    private val extSerializationService: ExtSerializationService,
    private val serializationService: SerializationService,
    private val filesResolver: FilesResolver,
) {
    suspend fun convert(
        accNo: String,
        version: Int,
        linkList: LinkList,
    ): ExtLinkList {
        val name = linkList.canonicalName
        val target = filesResolver.createRequestTempFile(accNo, version, name)
        val (file, links) = toExtLinks(accNo, linkList.file, SubFormat.fromFile(linkList.file), target)

        return ExtLinkList(name, file, links)
    }

    private suspend fun toExtLinks(
        accNo: String,
        source: File,
        format: SubFormat,
        target: File,
    ): Pair<File, List<ExtLink>> {
        val extLinks = mutableListOf<ExtLink>()

        suspend fun copy(
            input: InputStream,
            format: SubFormat,
            target: OutputStream,
        ) {
            val idx = AtomicInteger(0)
            val links =
                serializationService
                    .deserializeLinkListAsFlow(input, format)
                    .onEach { link -> logger.info { "$accNo, Mapping link ${idx.getAndIncrement()}, path='${link.url}'" } }
                    .map {
                        val link = ExtLink(it.url, it.attributes.toExtAttributes())
                        extLinks.add(link)
                        return@map link
                    }

            val serialized = extSerializationService.serializeLinks(links, target)
            if (serialized < 1) throw InvalidFileListException.emptyFileList(source.name)
        }

        logger.info { "$accNo, Started mapping/check link list ${source.name} of submission '$accNo'" }
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, format, output) }
        logger.info { "$accNo, Finished mapping/check link list ${source.name} of submission '$accNo'" }
        return target to extLinks
    }
}
