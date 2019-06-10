package ac.uk.ebi.biostd.files.exception

import ebi.ac.uk.security.integration.model.api.SecurityUser

class UserGroupNotFound(user: SecurityUser, groupName: String) : RuntimeException() {

    override val message: String = "User ${user.email} do not have access to group $groupName or group do not exists"
}
