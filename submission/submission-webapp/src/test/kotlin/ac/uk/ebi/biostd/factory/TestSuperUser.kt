package ac.uk.ebi.biostd.factory

object TestSuperUser : TestSecurityUser {
    override val email: String = "admin_user@ebi.ac.uk"
    override val fullName: String = "admin_user"
    override val superuser: Boolean = true
    override val notificationsEnabled: Boolean = false
}
