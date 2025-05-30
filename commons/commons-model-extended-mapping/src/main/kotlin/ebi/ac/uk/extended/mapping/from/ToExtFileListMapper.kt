package ebi.ac.uk.extended.mapping.from

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.use
import ebi.ac.uk.model.FileList
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

class ToExtFileListMapper(
    private val extSerializationService: ExtSerializationService,
    private val serializationService: SerializationService,
    private val filesResolver: FilesResolver,
) {
    suspend fun convert(
        accNo: String,
        version: Int,
        fileList: ExtFileList,
        fileSource: FileSourcesList,
    ): ExtFileList {
        val target = filesResolver.createRequestTempFile(accNo, version, fileList.fileName)
        val file = toExtFile(accNo, fileList, target, fileSource)
        return ExtFileList(fileList.filePath, file)
    }

    suspend fun convert(
        accNo: String,
        version: Int,
        fileList: FileList,
        fileSource: FileSourcesList,
    ): ExtFileList {
        val name = fileList.canonicalName
        val target = filesResolver.createRequestTempFile(accNo, version, name)
        val file = toExtFile(accNo, fileList.file, SubFormat.fromFile(fileList.file), target, fileSource)
        return ExtFileList(name, file)
    }

    private suspend fun toExtFile(
        accNo: String,
        fileList: ExtFileList,
        target: File,
        sources: FileSourcesList,
    ): File {
        suspend fun copy(
            input: InputStream,
            target: OutputStream,
        ) {
            val idx = AtomicInteger(0)
            val sourceFiles =
                extSerializationService
                    .deserializeListAsFlow(input)
                    .onEach { file -> logger.info { "$accNo, Mapping file ${idx.getAndIncrement()}, path='${file.filePath}'" } }
                    .map { sources.getExtFile(it) }
            val files = extSerializationService.serialize(sourceFiles, target)
            if (files < 1) throw InvalidFileListException.emptyFileList(fileList.fileName)
        }

        val source = fileList.file
        logger.info { "$accNo, Started mapping/check file list ${source.name} of submission '$accNo'" }
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, output) }
        logger.info { "$accNo, Finished mapping/check file list ${source.name} of submission '$accNo'" }
        return target
    }

    private suspend fun toExtFile(
        accNo: String,
        source: File,
        format: SubFormat,
        target: File,
        sources: FileSourcesList,
    ): File {
        suspend fun copy(
            input: InputStream,
            format: SubFormat,
            target: OutputStream,
        ) {
            val idx = AtomicInteger(0)
            val sourceFiles =
                serializationService
                    .deserializeFileListAsFlow(input, format)
                    .onEach { file -> logger.info { "$accNo, Mapping file ${idx.getAndIncrement()}, path='${file.path}'" } }
                    .map { sources.getExtFile(it) }
            val files = extSerializationService.serialize(sourceFiles, target)
            if (files < 1) throw InvalidFileListException.emptyFileList(source.name)
        }

        logger.info { "$accNo, Started mapping/check file list ${source.name} of submission '$accNo'" }
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, format, output) }
        logger.info { "$accNo, Finished mapping/check file list ${source.name} of submission '$accNo'" }
        return target
    }
}
