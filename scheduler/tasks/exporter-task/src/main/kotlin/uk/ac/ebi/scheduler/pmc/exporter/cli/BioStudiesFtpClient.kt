package uk.ac.ebi.scheduler.pmc.exporter.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.config.BUFFER_SIZE
import java.io.InputStream
import java.io.PrintWriter
import java.io.Writer

private val logger = KotlinLogging.logger {}

class BioStudiesFtpClient(
    private val ftpClient: FTPClient,
    private val appProperties: ApplicationProperties,
) {
    companion object {
        fun createFtpClient(appProperties: ApplicationProperties): BioStudiesFtpClient =
            BioStudiesFtpClient(
                ftpClient = ftpClient(),
                appProperties = appProperties,
            )

        fun ftpClient(): FTPClient =
            FTPClient().apply {
                bufferSize = BUFFER_SIZE
                addProtocolCommandListener(PrintCommandListener(PrintWriter(Writer.nullWriter())))
            }
    }

    suspend fun login() =
        withContext(Dispatchers.IO) {
            val ftpConfig = appProperties.ftp

            ftpClient.connect(ftpConfig.host, ftpConfig.port.toInt())
            ftpClient.login(ftpConfig.user, ftpConfig.password)
            ftpClient.enterLocalPassiveMode()
        }

    suspend fun logout() =
        withContext(Dispatchers.IO) {
            ftpClient.logout()
            ftpClient.disconnect()
        }

    suspend fun storeFile(
        path: String,
        content: InputStream,
    ) {
        withContext(Dispatchers.IO) { ftpClient.storeFile(path, content) }
    }
}
