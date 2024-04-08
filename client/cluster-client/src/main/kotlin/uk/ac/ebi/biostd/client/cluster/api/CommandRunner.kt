package uk.ac.ebi.biostd.client.cluster.api

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.Session
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CommandRunner(private val session: Session) {
    fun executeCommand(command: String): Pair<Int, String> {
        val channel = createChannel()
        channel.setCommand(command)
        channel.connect()
        val output = getOutput(channel.inputStream)
        channel.disconnect()
        return Pair(channel.exitStatus, output)
    }

    private fun getOutput(inputStream: InputStream) = IOUtils.toString(BufferedReader(InputStreamReader(inputStream)))

    private fun createChannel() = session.openChannel("exec") as ChannelExec
}
