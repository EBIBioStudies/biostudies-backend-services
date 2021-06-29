package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ebi.ac.uk.api.security.ProfileAuxInfo
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.security.integration.model.api.SecurityPermission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import java.util.ArrayList

class SecurityMapper {

    fun toUserProfile(userInfo: UserInfo): UserProfile = userInfo.let { (user, token) ->
        UserProfile(
            sessid = token,
            username = user.login,
            fullname = user.fullName,
            email = user.email,
            superuser = user.superuser,
            deny = emptyList(),
            allow = getAllow(user),
            secret = user.magicFolder.relativePath.toString(),
            aux = ProfileAuxInfo("")
        )
    }

    private fun getAllow(user: SecurityUser): List<String> {
        val accessTags = ArrayList<String>()
        accessTags.add(user.email)
        accessTags.add(PUBLIC_ACCESS_TAG.value)
        accessTags.addAll(getPermissions(user.permissions))
        return accessTags
    }

    private fun getPermissions(permissions: Set<SecurityPermission>) =
        permissions.filter { it.accessType == AccessType.READ }.map { it.accessTag }
}
