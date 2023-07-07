package ac.uk.ebi.biostd.itest.itest

import org.testcontainers.containers.FixedHostPortGenericContainer
import java.net.ServerSocket
import java.net.Socket

data class CreationParams(val user: String, val password: String, val version: String)

class FtpContainer(private val params: CreationParams) :
    FixedHostPortGenericContainer<FtpContainer>("stilliard/pure-ftpd") {
    override fun start() {
        val (p1, p2, p3, p4) = findPortRange()
        withFixedExposedPort(p1, p1)
        withFixedExposedPort(p2, p2)
        withFixedExposedPort(p3, p3)
        withFixedExposedPort(p4, p4)
        withExposedPorts(FTP_PORT)
            .withEnv("FTP_USER_NAME", params.user)
            .withEnv("FTP_USER_PASS", params.password)
            .withEnv("ADDED_FLAGS", "--tls=1")
            .withEnv("TLS_USE_DSAPRAM", "true")
            .withEnv("FTP_PASSIVE_PORTS", "$p1:$p4")
            .withEnv("PUBLICHOST", containerIpAddress)
            .withEnv("FTP_USER_HOME", "/home/ftpUser")
            .withEnv("TLS_CN", "localhost")
            .withEnv("TLS_ORG", "YourOrg")
            .withEnv("TLS_C", "DE")
        super.start()
    }

    fun getUrl(): String {
        return containerIpAddress
    }

    fun getFtpPort(): Int {
        return getMappedPort(FTP_PORT)
    }

    private companion object {
        const val FTP_PORT = 21

        fun findPortRange(): PortRange {
            val one = ServerSocket(0).use { socket -> socket.localPort }
            val two = one + 1
            val three = two + 1
            val four = three + 1
            return when {
                availablePort(two) && availablePort(three) && availablePort(four) -> PortRange(one, two, three, four)
                else -> findPortRange()
            }
        }

        private fun availablePort(port: Int): Boolean =
            runCatching {
                Socket("localhost", port).close()
                return false
            }.getOrElse { true }
    }

    private data class PortRange(val port1: Int, val port2: Int, val port3: Int, val port4: Int)
}
