package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.submission.config.GeneralConfig.Companion.FTP_IN_CLIENT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.biostd.ftp.FtpClient

@Configuration
class FileConfig {
    @Bean
    fun fileMapper() = FilesMapper()

    @Bean
    fun fileServiceFactory(
        @Qualifier(FTP_IN_CLIENT) ftpClient: FtpClient,
    ) = FileServiceFactory(ftpClient)
}
