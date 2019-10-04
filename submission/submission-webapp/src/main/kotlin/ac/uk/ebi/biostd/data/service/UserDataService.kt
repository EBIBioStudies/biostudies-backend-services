package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.model.UserData
import ac.uk.ebi.biostd.persistence.model.UserDataId
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import arrow.core.Option
import ebi.ac.uk.base.toOption

class UserDataService(private val dataRepository: UserDataDataRepository) {
    fun getUserData(userId: Long, key: String): Option<UserData> =
        dataRepository.findById(UserDataId(userId, key)).toOption()

    fun searchByKey(userId: Long, key: String): List<UserData> =
        dataRepository.findByUserAndKey(userId, key.toLowerCase())

    fun saveUserData(userId: Long, key: String, content: String): UserData {
        return dataRepository.save(UserData(userId, key, content))
    }

    fun delete(userId: Long, accNo: String) {
        dataRepository.deleteById(UserDataId(userId, accNo))
    }

    fun findAll(userId: Long): List<UserData> {
        return dataRepository.findByUserId(userId)
    }
}
