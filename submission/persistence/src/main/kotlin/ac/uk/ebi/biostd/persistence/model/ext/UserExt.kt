package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.UserGroup

fun DbUser.addGroup(userGroup: UserGroup): DbUser = also { groups.add(userGroup) }
fun DbUser.activated(): DbUser = also { active = true }
