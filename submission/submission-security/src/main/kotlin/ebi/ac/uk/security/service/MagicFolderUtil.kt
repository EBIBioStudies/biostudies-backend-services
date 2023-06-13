package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.security.integration.model.api.FtpMagicFolder
import ebi.ac.uk.security.integration.model.api.NfsMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.nio.file.Path
import java.nio.file.Paths

class MagicFolderUtil(
    private val securityProps: SecurityProperties,
) {
    fun createMagicFolder(user: SecurityUser) {
        when (user.magicFolder) {
            is FtpMagicFolder -> TODO()
            is NfsMagicFolder -> createNfsMagicFolder(user.email, user.magicFolder)
        }
    }

    private fun createNfsMagicFolder(email: String, magicFolder: NfsMagicFolder) {
        FileUtils.getOrCreateFolder(magicFolder.path.parent, RWX__X___)
        FileUtils.getOrCreateFolder(magicFolder.path, RWXRWX___)
        FileUtils.createSymbolicLink(symLinkPath(email), magicFolder.path, RWXRWX___)
    }

    private fun symLinkPath(userEmail: String): Path {
        val prefixFolder = userEmail.substring(0, 1).lowercase()
        return Paths.get("${securityProps.magicDirPath}/$prefixFolder/$userEmail")
    }
}
