package ebi.ac.uk.ftp

import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPool
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

/**
 * Pooled FTP client. Allows to re-use FTPClient instances so socket connections and ftp logging does not need to be
 * performed on each FTP operation.
 */
internal class FTPClientPool(
    private val ftpUser: String,
    private val ftpPassword: String,
    private val ftpUrl: String,
    private val ftpPort: Int,
    private val ftpRootPath: String,
    private val ftpClientPool: GenericObjectPool<FTPClient> =
        createFtpPool(
            ftpUser,
            ftpPassword,
            ftpUrl,
            ftpPort,
            ftpRootPath,
        ),
) {
    fun <T> execute(action: (FTPClient) -> T): T {
        val ftpClient = ftpClientPool.borrowObject()
        return try {
            action(ftpClient)
        } finally {
            ftpClientPool.returnObject(ftpClient)
        }
    }

    private class FTPClientFactory(
        private val ftpUser: String,
        private val ftpPassword: String,
        private val ftpUrl: String,
        private val ftpPort: Int,
        private val ftpRootPath: String,
    ) : BasePooledObjectFactory<FTPClient>() {
        override fun create(): FTPClient {
            val ftp = ftpClient(3000.milliseconds, 3000.milliseconds)
            logger.info { "Connecting to $ftpUrl, $ftpPort" }
            ftp.connect(ftpUrl, ftpPort)
            ftp.login(ftpUser, ftpPassword)
            ftp.changeWorkingDirectory(ftpRootPath)
            ftp.listHiddenFiles = true
            ftp.enterLocalPassiveMode()
            return ftp
        }

        override fun wrap(ftpClient: FTPClient): PooledObject<FTPClient> {
            return DefaultPooledObject(ftpClient)
        }

        override fun destroyObject(p: PooledObject<FTPClient>) {
            val ftpClient = p.`object`
            if (ftpClient.isConnected) {
                ftpClient.logout()
                ftpClient.disconnect()
            }
        }

        @Suppress("TooGenericExceptionCaught")
        override fun validateObject(p: PooledObject<FTPClient>): Boolean {
            val ftpClient = p.`object`
            return try {
                ftpClient.sendNoOp()
            } catch (exception: Exception) {
                logger.error { "Error checking ftp connection: ${ exception.localizedMessage }" }
                false
            }
        }
    }

    private companion object {
        private const val MIN_CONNECTION = 2

        fun createFtpPool(
            ftpUser: String,
            ftpPassword: String,
            ftpUrl: String,
            ftpPort: Int,
            ftpRootPath: String,
        ): GenericObjectPool<FTPClient> {
            val factory = FTPClientFactory(ftpUser, ftpPassword, ftpUrl, ftpPort, ftpRootPath)
            var connections = GenericObjectPool(factory)
            connections.minIdle = MIN_CONNECTION
            connections.testOnBorrow = true
            return connections
        }
    }
}
