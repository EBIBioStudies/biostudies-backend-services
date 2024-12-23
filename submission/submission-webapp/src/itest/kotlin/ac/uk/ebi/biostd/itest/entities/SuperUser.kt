package ac.uk.ebi.biostd.itest.entities

import ac.uk.ebi.biostd.common.properties.StorageMode

/**
 * Represents a bio studies super user.
 */
object SuperUser : TestUser {
    override val username = "Super User"
    override val email = "biostudies-mgmt@ebi.ac.uk"
    override val password = "12345"
    override val superUser = true
    override val storageMode = StorageMode.NFS
}

object FtpSuperUser : TestUser {
    override val username = "Super User Fto"
    override val email = "biostudies-mgmt-ftp@ebi.ac.uk"
    override val password = "12345"
    override val superUser = true
    override val storageMode = StorageMode.FTP
}
