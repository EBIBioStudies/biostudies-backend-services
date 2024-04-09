package uk.ac.ebi.scheduler.pmc.exporter.cli

import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import java.io.InputStream

private val logger = KotlinLogging.logger {}

class BioStudiesFtpClient(
    private val ftpClient: FTPClient,
    private val appProperties: ApplicationProperties,
) {
    fun login() {
        val ftpConfig = appProperties.ftp

        ftpClient.connect(ftpConfig.host, ftpConfig.port.toInt())
        ftpClient.login(ftpConfig.user, ftpConfig.password)
        ftpClient.enterLocalPassiveMode()
    }

    fun logout() {
        ftpClient.logout()
        ftpClient.disconnect()
    }

    fun storeFile(
        path: String,
        content: InputStream,
    ) {
        try {
            ftpClient.storeFile(path, content)
        } catch (exception: FTPConnectionClosedException) {
            logger.error { "FTP connection timeout: ${exception.message}. Attempting reconnection" }

            reconnect()
            ftpClient.storeFile(path, content)
        }
    }

    private fun reconnect() {
        ftpClient.disconnect()
        login()
    }
}
