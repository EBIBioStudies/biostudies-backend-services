package ebi.ac.uk.ftp

import java.nio.file.Path

fun FtpClient.exists(ftpPath: Path): Boolean {
    return listFiles(ftpPath).isNotEmpty()
}
