package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.UserGroup

fun User.addGroup(userGroup: UserGroup): User = also { groups.add(userGroup) }
fun User.activated(): User = also { active = true }
