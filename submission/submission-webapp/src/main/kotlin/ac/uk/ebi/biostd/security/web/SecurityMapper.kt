package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.ext.magicFolterRelativePath
import ebi.ac.uk.api.security.ProfileAuxInfo
import ebi.ac.uk.api.security.RegisterResponse
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.security.integration.model.api.UserInfo
import java.util.ArrayList

class SecurityMapper {

    fun toSignUpResponse(user: User) = RegisterResponse(user.login)

    fun toUserProfile(userInfo: UserInfo): UserProfile = userInfo.let { (user, token) ->
        UserProfile(
            sessid = token,
            username = user.login,
            fullname = user.fullName,
            email = user.email,
            superuser = user.superuser,
            deny = emptyList(),
            allow = getAllow(user),
            secret = user.magicFolterRelativePath,
            aux = ProfileAuxInfo(user.auxInfo["orcid"]))
    }

    private fun getAllow(user: User): List<String> {
        val accessTags = ArrayList<String>()
        accessTags.add("~" + user.email)
        accessTags.add("#" + user.id)
        accessTags.add("Public")
        accessTags.addAll(getPermissions(user.permissions))
        return accessTags
    }

    private fun getPermissions(permissions: Set<AccessPermission>) =
        permissions.filter { it.accessType == AccessType.READ }.map { it.accessTag.name }
}
