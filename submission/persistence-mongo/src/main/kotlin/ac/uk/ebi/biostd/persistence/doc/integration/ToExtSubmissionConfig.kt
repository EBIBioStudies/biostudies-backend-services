package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver

@Configuration
class ToExtSubmissionConfig {
    @Bean
    internal fun toExtFileListMapper(
        fileListDocFileRepository: FileListDocFileRepository,
        extSerializationService: ExtSerializationService,
        extFilesResolver: FilesResolver,
    ): ToExtFileListMapper = ToExtFileListMapper(fileListDocFileRepository, extSerializationService, extFilesResolver)

    @Bean
    internal fun toExtSectionMapper(
        toExtFileListMapper: ToExtFileListMapper,
    ): ToExtSectionMapper = ToExtSectionMapper(toExtFileListMapper)

    @Bean
    internal fun toExtSubmissionMapper(
        toExtSectionMapper: ToExtSectionMapper,
    ): ToExtSubmissionMapper = ToExtSubmissionMapper(toExtSectionMapper)
}
