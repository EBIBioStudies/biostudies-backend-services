package uk.ac.ebi.biostd.itest.entities

import uk.ac.ebi.biostd.common.properties.StorageMode

object ExistingUser : TestUser {
    override val username = "Register User"
    override val email = "register_user@ebi.ac.uk"
    override val password = "1234"
    override val superUser = false
    override val storageMode = StorageMode.NFS
}
