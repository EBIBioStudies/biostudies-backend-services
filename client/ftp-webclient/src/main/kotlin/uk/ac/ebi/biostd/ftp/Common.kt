package uk.ac.ebi.biostd.ftp

import org.apache.commons.net.ftp.FTPClient
import kotlin.time.Duration

fun ftpClient(
    connectTimeout: Duration,
    defaultTimeout: Duration,
): FTPClient =
    FTPClient().apply {
        this.connectTimeout = connectTimeout.inWholeMilliseconds.toInt()
        this.defaultTimeout = defaultTimeout.inWholeMilliseconds.toInt()
    }
