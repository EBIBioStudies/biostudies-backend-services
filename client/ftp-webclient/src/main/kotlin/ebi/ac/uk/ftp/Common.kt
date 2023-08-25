package ebi.ac.uk.ftp

import org.apache.commons.net.ftp.FTPSClient
import kotlin.time.Duration

fun ftpClient(connectTimeout: Duration, defaultTimeout: Duration): FTPSClient {
    return FTPSClient().apply {
        this.connectTimeout = connectTimeout.inWholeMilliseconds.toInt()
        this.defaultTimeout = defaultTimeout.inWholeMilliseconds.toInt()
    }
}
