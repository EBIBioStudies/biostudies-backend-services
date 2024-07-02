package uk.ac.ebi.biostd.client.cluster.api

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SshClient private constructor(
    private val sshMachine: String,
    private val jSch: JSch,
) {
    constructor(sshMachine: String, sshKey: String) : this(sshMachine, JSch().apply { addIdentity(sshKey) })

    internal suspend fun <T> runInSession(exec: suspend CommandRunner.() -> T): T {
        fun createSession(): Session {
            val session = jSch.getSession(sshMachine)
            session.setConfig("StrictHostKeyChecking", "no")
            return session
        }

        return withContext(Dispatchers.IO) {
            val session = createSession()

            try {
                session.connect()
                exec(CommandRunner(session))
            } finally {
                session.disconnect()
            }
        }
    }
}
