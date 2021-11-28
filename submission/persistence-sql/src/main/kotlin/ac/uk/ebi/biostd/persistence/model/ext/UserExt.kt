package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup

fun DbUser.addGroup(userGroup: DbUserGroup): DbUser = also { groups.add(userGroup) }
fun DbUser.activated(): DbUser = also { active = true }
