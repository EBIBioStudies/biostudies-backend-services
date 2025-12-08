package ac.uk.ebi.biostd.itest.entities

import ac.uk.ebi.biostd.common.properties.StorageMode

object ExistingUser : TestUser {
    override val username = "Register User"
    override val email = "register_user@ebi.ac.uk"
    override val password = "1234"
    override val superUser = false
    override val storageMode = StorageMode.NFS
}
