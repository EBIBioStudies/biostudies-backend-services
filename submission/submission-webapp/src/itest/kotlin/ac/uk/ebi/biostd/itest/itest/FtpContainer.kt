package ac.uk.ebi.biostd.itest.itest

import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.net.ServerSocket

data class CreationParams(val user: String, val password: String, val version: String)

class FtpContainer(private val params: CreationParams) :
    FixedHostPortGenericContainer<FtpContainer>("delfer/alpine-ftp-server:${params.version}") {
    override fun start() {
        val freePort = ServerSocket(0).use { socket -> socket.localPort }
        withFixedExposedPort(freePort, freePort)
            .withEnv("MIN_PORT", freePort.toString())
            .withEnv("MAX_PORT", freePort.toString())
            .withExposedPorts(21)
            .withEnv("USERS", "${params.user}|${params.password}")
            .waitingFor(FtpPortWaitStrategy)
        super.start()
    }

    fun getUrl(): String {
        return containerIpAddress
    }

    fun getFtpPort(): Int {
        return getMappedPort(21)
    }
}

object FtpPortWaitStrategy : HostPortWaitStrategy() {
    override fun getLivenessCheckPorts(): Set<Int> {
        return setOf(waitStrategyTarget.getMappedPort(21))
    }
}
