package ebi.ac.uk.security.util

import java.security.MessageDigest
import java.util.Arrays

class PasswordVerifier(private val tokenUtil: TokenUtil) {

    fun checkPassword(passwordDigest: ByteArray, password: String): Boolean {
        val tokenUser = tokenUtil.fromToken(password)
        val isValidSuperUser = tokenUser.fold({ false }, { it.superuser })
        val isValidRegularUser = Arrays.equals(this.getPasswordDigest(password), passwordDigest)
        return isValidSuperUser || isValidRegularUser
    }

    fun getPasswordDigest(password: String) = MessageDigest.getInstance("SHA1").digest(password.toByteArray())!!
}
