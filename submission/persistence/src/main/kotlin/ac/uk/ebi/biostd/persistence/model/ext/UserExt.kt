package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.UserGroup

fun User.addGroup(userGroup: UserGroup): User {
    groups.add(userGroup)
    return this
}

fun User.activate(activationKey: String): User {
    this.activationKey = activationKey
    return this
}

fun User.activated(): User {
    this.active = true
    return this
}

val User.magicFolterRelativePath: String
    get() {
        return "${secret.dropLast(2)}/${secret.takeLast(2)}-a$id"
    }
