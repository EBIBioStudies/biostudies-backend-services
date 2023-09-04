package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.FilesProperties
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
    fun ftpClient(properties: ApplicationProperties) = ftpClient(properties.security.filesProperties)

    @Bean
    fun fileServiceFactory(ftpClient: FtpClient) = FileServiceFactory(ftpClient)

    companion object {
        fun ftpClient(fileProperties: FilesProperties): FtpClient {
            return FtpClient.create(
                ftpUser = fileProperties.ftpUser,
                ftpPassword = fileProperties.ftpPassword,
                ftpUrl = fileProperties.ftpUrl,
                ftpPort = fileProperties.ftpPort,
            )
        }
    }
}
