package ebi.ac.uk.ftp

import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.DataConnectionConfigurationFactory
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.impl.DefaultFtpServer
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class FtpServer(
    private val server: DefaultFtpServer,
    val fileSystemDirectory: File,
) {
    fun start() {
        Thread { server.start() }.start()
        while (server.isStopped) Thread.sleep(50)
    }

    fun stop() {
        server.stop()
    }

    fun getUrl(): String = "localhost"

    val ftpPort: Int
        get() =
            server.serverContext.listeners
                .getValue(LISTENER_NAME)
                .port

    companion object {
        const val LISTENER_NAME = "default"

        fun createServer(config: FtpConfig): FtpServer {
            val listenerFactory = ListenerFactory()
            listenerFactory.port = 0
            listenerFactory.isImplicitSsl = false

            // Enable ssl
            val ssl = SslConfigurationFactory()
            ssl.setSslProtocol("TLSv1.3")
            ssl.keystoreFile = config.sslConfig.keystoreFile
            ssl.keystorePassword = config.sslConfig.password
            listenerFactory.sslConfiguration = ssl.createSslConfiguration()

            // Enable passive mode
            val dataConnFactory = DataConnectionConfigurationFactory()
            dataConnFactory.isImplicitSsl = false
            dataConnFactory.passiveExternalAddress = "127.0.0.1"
            listenerFactory.dataConnectionConfiguration = dataConnFactory.createDataConnectionConfiguration()

            // Configure maximum connections
            val serverFactory = FtpServerFactory()
            val connectionConfigFactory = ConnectionConfigFactory()
            connectionConfigFactory.maxLogins = 50
            serverFactory.connectionConfig = connectionConfigFactory.createConnectionConfig()

            serverFactory.addListener(LISTENER_NAME, listenerFactory.createListener())
            val server = serverFactory.createServer()
            val user = newUser(config.userName, config.password, config.path)
            serverFactory.userManager.save(user)
            return FtpServer(server as DefaultFtpServer, File(user.homeDirectory))
        }

        private fun newUser(
            userName: String,
            userPassword: String,
            path: Path,
        ): BaseUser =
            BaseUser().apply {
                name = userName
                password = userPassword
                homeDirectory = path.absolutePathString()
                authorities = listOf(WritePermission())
            }
    }
}

data class SslConfig(
    val keystoreFile: File,
    val password: String,
)

data class FtpConfig(
    val sslConfig: SslConfig,
    val userName: String,
    val password: String,
    val path: Path,
)
