package ac.uk.ebi.biostd.itest.itest

import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.DataConnectionConfigurationFactory
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.impl.DefaultFtpServer
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString

class FtpServer(
    private val server: DefaultFtpServer,
) {

    fun start() {
        Thread { server.start() }.start()
        while (server.isStopped) Thread.sleep(50)
    }

    fun stop() {
        server.stop()
    }

    fun getUrl(): String {
        return "localhost"
    }

    val ftpPort: Int
        get() = server.serverContext.listeners.getValue(listenerName).port

    companion object {
        const val listenerName = "default"

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

            serverFactory.addListener(listenerName, listenerFactory.createListener())
            val server = serverFactory.createServer()
            serverFactory.userManager.save(newUser(config.userName, config.password))
            return FtpServer(server as DefaultFtpServer)
        }

        private fun newUser(
            userName: String,
            userPassword: String,
        ): BaseUser {
            return BaseUser().apply {
                name = userName
                password = userPassword
                homeDirectory = Files.createTempDirectory("$userName-ftp").absolutePathString()
                authorities = listOf(WritePermission())
            }
        }
    }
}

data class SslConfig(val keystoreFile: File, val password: String)
data class FtpConfig(
    val sslConfig: SslConfig,
    val userName: String,
    val password: String,
)
