package ac.uk.ebi.biostd.itest.itest

import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.net.ServerSocket
import java.net.Socket
import java.time.Duration

data class CreationParams(val user: String, val password: String, val version: String)

class FtpContainer(private val params: CreationParams) :
    FixedHostPortGenericContainer<FtpContainer>("stilliard/pure-ftpd") {
    override fun start() {
        val freePort = findPortRange()
        withFixedExposedPort(freePort, freePort)
        withFixedExposedPort(freePort + 1, freePort + 1)
        withFixedExposedPort(freePort + 2, freePort + 2)
        withFixedExposedPort(freePort + 3, freePort + 3)
        withExposedPorts(21)
            .withEnv("FTP_USER_NAME", params.user)
            .withEnv("FTP_USER_PASS", params.password)
            .withEnv("ADDED_FLAGS", "--tls=1")
            .withEnv("TLS_USE_DSAPRAM", "true")
            .withEnv("FTP_PASSIVE_PORTS", "$freePort:${freePort + 3}")
            .withEnv("PUBLICHOST", "0.0.0.0")
            .withEnv("FTP_USER_HOME", "/home/ftpUser")
            .withEnv("TLS_CN", "localhost")
            .withEnv("TLS_ORG", "YourOrg")
            .withEnv("TLS_C", "DE")
            .waitingFor(FtpPortWaitStrategy)
        super.start()
    }

    fun getUrl(): String {
        return "0.0.0.0"
    }

    fun getFtpPort(): Int {
        return getMappedPort(21)
    }
}

fun findPortRange(): Int {
    val one = ServerSocket(0).use { socket -> socket.localPort }
    val two = one + 1
    val three = two + 1
    val four = three + 1
    return if (availablePort(two) && availablePort(three) && availablePort(four)) one else findPortRange()
}

private fun availablePort(port: Int): Boolean =
    runCatching { Socket("localhost", port).close(); return false }.getOrElse { true }

object FtpPortWaitStrategy : HostPortWaitStrategy() {
    init {
        startupTimeout = Duration.ofMinutes(10)
    }

    override fun getLivenessCheckPorts(): Set<Int> = setOf(waitStrategyTarget.getMappedPort(21))
}
