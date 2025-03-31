package ebi.ac.uk.extended.mapping.to

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.io.use
import ebi.ac.uk.model.LinkList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("TooManyFunctions")
class ToLinkListMapper(
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
    private val filesResolver: FilesResolver,
) {
    suspend fun convert(linkList: ExtLinkList): LinkList = LinkList(linkList.filePath, emptyFile(linkList.fileName))

    suspend fun serialize(
        linkList: ExtLinkList,
        targetFormat: SubFormat,
        file: File,
    ): File {
        toFile(linkList.file, targetFormat, file)
        return file
    }

    suspend fun serialize(
        fileListFiles: Flow<ExtLink>,
        targetFormat: SubFormat,
        file: File,
    ): File {
        toFile(fileListFiles, targetFormat, file)
        return file
    }

    private suspend fun emptyFile(fileName: String): File {
        val targetFile = filesResolver.createTempFile(fileName = fileName)
        targetFile.outputStream().use { serializationService.serializeLinkList(emptyFlow(), SubFormat.JSON, it) }
        return targetFile
    }

    private suspend fun toFile(
        source: File,
        targetFormat: SubFormat,
        target: File,
    ): File {
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, targetFormat, output) }
        return target
    }

    private suspend fun toFile(
        source: Flow<ExtLink>,
        targetFormat: SubFormat,
        target: File,
    ): File {
        target.outputStream().use { copy(source, targetFormat, it) }
        return target
    }

    private suspend fun copy(
        source: Flow<ExtLink>,
        targetFormat: SubFormat,
        target: OutputStream,
    ) {
        val sourceLinks = source.map { it.toLink() }
        serializationService.serializeLinkList(sourceLinks, targetFormat, target)
    }

    private suspend fun copy(
        input: InputStream,
        targetFormat: SubFormat,
        target: OutputStream,
    ) {
        val sourceLinks = extSerializationService.deserializeLinkListAsFlow(input).map { it.toLink() }
        serializationService.serializeLinkList(sourceLinks, targetFormat, target)
    }
}
