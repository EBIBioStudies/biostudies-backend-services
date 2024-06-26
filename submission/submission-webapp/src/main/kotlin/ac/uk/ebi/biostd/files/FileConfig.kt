package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ebi.ac.uk.ftp.FtpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FileConfig {
    @Bean
    fun fileMapper() = FilesMapper()

    @Bean
    fun fileServiceFactory(ftpClient: FtpClient) = FileServiceFactory(ftpClient)
}
