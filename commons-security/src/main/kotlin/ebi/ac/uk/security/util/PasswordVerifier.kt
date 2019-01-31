package ebi.ac.uk.security.util

import java.security.MessageDigest
import java.util.Arrays

private val sha1: MessageDigest = MessageDigest.getInstance("SHA1")

class PasswordVerifier(private val tokenUtil: TokenUtil) {

    fun checkPassword(passwordDigest: ByteArray, password: String): Boolean {
        val tokenUser = tokenUtil.fromToken(password)
        val isValidSuperUser = tokenUser.isDefined() && tokenUser.get().superuser
        val isValidRegularUser = Arrays.equals(this.getPasswordDigest(password), passwordDigest)
        return isValidSuperUser || isValidRegularUser
    }

    fun getPasswordDigest(password: String) = sha1.digest(password.toByteArray())!!
}
