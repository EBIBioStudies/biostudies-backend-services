package ac.uk.ebi.biostd.itest.itest

import org.testcontainers.containers.FixedHostPortGenericContainer

data class CreationParams(val user: String, val password: String, val version: String)

class FtpContainer(private val params: CreationParams) :
    FixedHostPortGenericContainer<FtpContainer>("delfer/alpine-ftp-server:${params.version}") {
    override fun start() {
        withFixedExposedPort(21000, 21000)
            .withEnv("MIN_PORT", "21000")
            .withEnv("MAX_PORT", "21000")
            .withExposedPorts(21)
            .withEnv("USERS", "${params.user}|${params.password}")
        super.start()
    }

    fun getUrl(): String {
        return containerIpAddress
    }

    fun getFtpPort(): Int {
        return getMappedPort(21)
    }
}
