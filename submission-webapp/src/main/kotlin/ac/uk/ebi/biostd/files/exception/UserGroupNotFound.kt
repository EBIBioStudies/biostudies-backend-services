package ac.uk.ebi.biostd.files.exception

import ebi.ac.uk.model.User

class UserGroupNotFound(user: User, groupName: String) : RuntimeException() {

    override val message: String = "User ${user.email} do not have access to group $groupName or group do not exists"
}
