package ebi.ac.uk.extended.mapping.from

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkList
import uk.ac.ebi.biostd.io.use
import uk.ac.ebi.biostd.model.LinkList
import uk.ac.ebi.biostd.model.canonicalName
import uk.ac.ebi.biostd.serialization.common.FilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
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
        linkList: ExtLinkList,
    ): ExtLinkList {
        val target = filesResolver.createRequestTempFile(accNo, version, linkList.fileName)
        val file = toExtFile(accNo, linkList, target)
        return ExtLinkList(linkList.filePath, file)
    }

    suspend fun convert(
        accNo: String,
        version: Int,
        linkList: LinkList,
    ): ExtLinkList {
        val name = linkList.canonicalName
        val target = filesResolver.createRequestTempFile(accNo, version, name)
        val (file, links) = toExtFile(accNo, linkList.file, SubFormat.fromFile(linkList.file), target)

        return ExtLinkList(name, file, links)
    }

    private suspend fun toExtFile(
        accNo: String,
        linkList: ExtLinkList,
        target: File,
    ): File {
        suspend fun copy(
            input: InputStream,
            target: OutputStream,
        ) {
            val idx = AtomicInteger(0)
            val links =
                extSerializationService
                    .deserializeLinkListAsFlow(input)
                    .onEach { link -> logger.info { "$accNo, Mapping link ${idx.getAndIncrement()}, path='${link.url}'" } }

            val serialized = extSerializationService.serializeLinks(links, target)
            if (serialized < 1) throw InvalidFileListException.emptyFileList(linkList.fileName)
        }

        val source = linkList.file
        logger.info { "$accNo, Started mapping/check link list ${source.name} of submission '$accNo'" }
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, output) }
        logger.info { "$accNo, Finished mapping/check link list ${source.name} of submission '$accNo'" }
        return target
    }

    private suspend fun toExtFile(
        accNo: String,
        source: File,
        format: SubFormat,
        target: File,
    ): Pair<File, List<ExtLink>> {
        suspend fun copy(
            input: InputStream,
            format: SubFormat,
            target: OutputStream,
        ): List<ExtLink> {
            val idx = AtomicInteger(0)
            val links =
                serializationService
                    .deserializeLinkListAsFlow(input, format)
                    .onEach { link -> logger.info { "$accNo, Mapping link ${idx.getAndIncrement()}, path='${link.url}'" } }
                    .map {
                        ExtLink(it.url, it.attributes.toExtAttributes())
                    }.toList()

            val serialized = extSerializationService.serializeLinks(links.asFlow(), target)
            if (serialized < 1) throw InvalidFileListException.emptyFileList(source.name)
            return links
        }

        logger.info { "$accNo, Started mapping/check link list ${source.name} of submission '$accNo'" }
        val extLinks = use(source.inputStream(), target.outputStream()) { input, output -> copy(input, format, output) }
        logger.info { "$accNo, Finished mapping/check link list ${source.name} of submission '$accNo'" }
        return target to extLinks
    }
}
